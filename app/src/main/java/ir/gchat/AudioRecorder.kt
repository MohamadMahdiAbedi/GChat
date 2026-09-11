package ir.gchat

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.MediaMuxer
import android.media.MediaRecorder
import android.os.Build
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File

class AudioRecorder(
    private val context: Context
) {

    companion object {
        private const val SAMPLE_RATE = 44_100
        private const val CHANNEL_COUNT = 1
        private const val BIT_RATE = 128_000

        private const val MIME_TYPE = "audio/mp4a-latm"
        private const val TIMEOUT_US = 10_000L
    }

    enum class RecordingState {
        IDLE,
        RECORDING,
        PAUSED
    }

    private val _state = MutableStateFlow(
        RecordingState.IDLE
    )

    val state = _state.asStateFlow()

    private var audioRecord: AudioRecord? = null
    private var encoder: MediaCodec? = null
    private var muxer: MediaMuxer? = null

    private var file: File? = null

    @Volatile
    private var recording = false

    @Volatile
    private var paused = false

    private var recordingThread: Thread? = null

    @Volatile
    private var muxerStarted = false

    private var trackIndex = -1

    private var totalSamples = 0L

    fun start(): File {

        check(!recording) {
            "Recorder is already running"
        }

        check(hasRecordPermission()) {
            "RECORD_AUDIO permission is not granted"
        }

        val directory = File(
            context.filesDir,
            "recordings"
        ).apply {
            if (!exists()) {
                mkdirs()
            }
        }

        val output = File(
            directory,
            "voice_${System.currentTimeMillis()}.m4a"
        )

        val minBufferSize = AudioRecord.getMinBufferSize(
            SAMPLE_RATE,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )

        require(minBufferSize > 0) {
            "Unable to get AudioRecord buffer size"
        }

        val bufferSize = maxOf(
            minBufferSize,
            SAMPLE_RATE / 10 * 2
        )

        val record: AudioRecord

        try {

            record = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferSize
            )

            check(
                record.state == AudioRecord.STATE_INITIALIZED
            ) {
                record.release()
                "Unable to initialize AudioRecord"
            }

            // فقط یک بار
            record.startRecording()

            check(
                record.recordingState ==
                        AudioRecord.RECORDSTATE_RECORDING
            ) {
                record.release()
                "Unable to start AudioRecord"
            }

        } catch (e: SecurityException) {
            throw SecurityException(
                "RECORD_AUDIO permission is required",
                e
            )
        }

        val codec = try {

            MediaCodec.createEncoderByType(
                MIME_TYPE
            )

        } catch (e: Exception) {

            record.release()
            output.delete()

            throw e
        }

        val format = MediaFormat.createAudioFormat(
            MIME_TYPE,
            SAMPLE_RATE,
            CHANNEL_COUNT
        ).apply {

            setInteger(
                MediaFormat.KEY_AAC_PROFILE,
                MediaCodecInfo.CodecProfileLevel.AACObjectLC
            )

            setInteger(
                MediaFormat.KEY_BIT_RATE,
                BIT_RATE
            )

            setInteger(
                MediaFormat.KEY_MAX_INPUT_SIZE,
                bufferSize
            )
        }

        try {

            codec.configure(
                format,
                null,
                null,
                MediaCodec.CONFIGURE_FLAG_ENCODE
            )

        } catch (e: Exception) {

            codec.release()
            record.release()
            output.delete()

            throw e
        }

        val mediaMuxer = try {

            MediaMuxer(
                output.absolutePath,
                MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4
            )

        } catch (e: Exception) {

            codec.release()
            record.release()
            output.delete()

            throw e
        }

        audioRecord = record
        encoder = codec
        muxer = mediaMuxer
        file = output

        recording = true
        paused = false

        muxerStarted = false
        trackIndex = -1
        totalSamples = 0L

        // وضعیت UI
        _state.value = RecordingState.RECORDING

        try {

            codec.start()

        } catch (e: Exception) {

            recording = false
            paused = false

            _state.value = RecordingState.IDLE

            codec.release()
            record.release()
            mediaMuxer.release()

            audioRecord = null
            encoder = null
            muxer = null
            file = null

            output.delete()

            throw e
        }

        recordingThread = Thread {

            try {

                encodeLoop(
                    record = record,
                    codec = codec,
                    mediaMuxer = mediaMuxer,
                    bufferSize = bufferSize
                )

            } catch (_: Throwable) {

                output.delete()

            } finally {

                try {
                    record.stop()
                } catch (_: Exception) {
                }

                try {
                    record.release()
                } catch (_: Exception) {
                }

                try {
                    codec.stop()
                } catch (_: Exception) {
                }

                try {
                    codec.release()
                } catch (_: Exception) {
                }

                if (muxerStarted) {
                    try {
                        mediaMuxer.stop()
                    } catch (_: Exception) {
                    }
                }

                try {
                    mediaMuxer.release()
                } catch (_: Exception) {
                }

                audioRecord = null
                encoder = null
                muxer = null

                recording = false
                paused = false

                // اگر stop/cancel قبلاً state را IDLE کرده،
                // دوباره چیزی لازم نیست.
                _state.value = RecordingState.IDLE
            }
        }

        recordingThread?.start()

        return output
    }

    private fun encodeLoop(
        record: AudioRecord,
        codec: MediaCodec,
        mediaMuxer: MediaMuxer,
        bufferSize: Int
    ) {

        val pcmBuffer = ByteArray(bufferSize)

        var inputFinished = false
        var outputFinished = false

        while (!outputFinished) {

            if (!paused && !inputFinished) {

                val inputIndex =
                    codec.dequeueInputBuffer(TIMEOUT_US)

                if (inputIndex >= 0) {

                    val inputBuffer =
                        codec.getInputBuffer(inputIndex)

                    if (inputBuffer != null) {

                        inputBuffer.clear()

                        val readSize = record.read(
                            pcmBuffer,
                            0,
                            pcmBuffer.size
                        )

                        if (readSize > 0) {

                            inputBuffer.put(
                                pcmBuffer,
                                0,
                                readSize
                            )

                            val presentationTimeUs =
                                totalSamples *
                                        1_000_000L /
                                        SAMPLE_RATE

                            totalSamples +=
                                readSize / 2L

                            codec.queueInputBuffer(
                                inputIndex,
                                0,
                                readSize,
                                presentationTimeUs,
                                0
                            )
                        }
                    }
                }
            }

            if (!recording && !inputFinished) {

                val inputIndex =
                    codec.dequeueInputBuffer(TIMEOUT_US)

                if (inputIndex >= 0) {

                    val presentationTimeUs =
                        totalSamples *
                                1_000_000L /
                                SAMPLE_RATE

                    codec.queueInputBuffer(
                        inputIndex,
                        0,
                        0,
                        presentationTimeUs,
                        MediaCodec.BUFFER_FLAG_END_OF_STREAM
                    )

                    inputFinished = true
                }
            }

            val bufferInfo =
                MediaCodec.BufferInfo()

            while (true) {

                val outputIndex =
                    codec.dequeueOutputBuffer(
                        bufferInfo,
                        TIMEOUT_US
                    )

                when {

                    outputIndex ==
                            MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {

                        if (muxerStarted) {
                            throw IllegalStateException(
                                "Encoder output format changed twice"
                            )
                        }

                        val outputFormat =
                            codec.outputFormat

                        trackIndex =
                            mediaMuxer.addTrack(
                                outputFormat
                            )

                        mediaMuxer.start()

                        muxerStarted = true
                    }

                    outputIndex ==
                            MediaCodec.INFO_TRY_AGAIN_LATER -> {

                        break
                    }

                    outputIndex >= 0 -> {

                        val outputBuffer =
                            codec.getOutputBuffer(
                                outputIndex
                            )

                        if (
                            outputBuffer != null &&
                            bufferInfo.size > 0
                        ) {

                            check(muxerStarted) {
                                "Muxer has not started"
                            }

                            outputBuffer.position(
                                bufferInfo.offset
                            )

                            outputBuffer.limit(
                                bufferInfo.offset +
                                        bufferInfo.size
                            )

                            mediaMuxer.writeSampleData(
                                trackIndex,
                                outputBuffer,
                                bufferInfo
                            )
                        }

                        val endOfStream =
                            bufferInfo.flags and
                                    MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0

                        codec.releaseOutputBuffer(
                            outputIndex,
                            false
                        )

                        if (endOfStream) {
                            outputFinished = true
                            break
                        }
                    }
                }
            }
        }
    }

    fun pause() {

        check(recording) {
            "Recorder is not running"
        }

        if (paused) {
            return
        }

        paused = true

        _state.value =
            RecordingState.PAUSED
    }

    fun resume() {

        check(recording) {
            "Recorder is not running"
        }

        if (!paused) {
            return
        }

        paused = false

        _state.value =
            RecordingState.RECORDING
    }

    fun stop(): File? {

        if (!recording) {
            return null
        }

        recording = false
        paused = false

        // بلافاصله UI را به IDLE ببر
        _state.value =
            RecordingState.IDLE

        try {
            recordingThread?.join()
        } catch (_: InterruptedException) {
            Thread.currentThread().interrupt()
        }

        recordingThread = null

        val output = file

        audioRecord = null
        encoder = null
        muxer = null
        file = null

        return if (
            output != null &&
            output.exists() &&
            output.length() > 0L
        ) {
            output
        } else {

            output?.delete()

            null
        }
    }

    fun cancel() {

        recording = false
        paused = false

        _state.value =
            RecordingState.IDLE

        try {
            recordingThread?.join()
        } catch (_: InterruptedException) {
            Thread.currentThread().interrupt()
        }

        recordingThread = null

        file?.delete()

        audioRecord = null
        encoder = null
        muxer = null
        file = null
    }

    fun isRecording(): Boolean {
        return recording
    }

    fun isPaused(): Boolean {
        return recording && paused
    }

    private fun hasRecordPermission(): Boolean {

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            context.checkSelfPermission(
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED

        } else {

            true
        }
    }
}
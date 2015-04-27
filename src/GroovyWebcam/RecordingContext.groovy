/*
@author - https://github.com/VikramN/

LICENSE
WebcamGroovy => MIT / GPL
JavaCv https://github.com/bytedeco/javacv => GPL
FFMpeg https://www.ffmpeg.org => GPL / LGPL
OpenCv http://opencv.org/ => 3-clause BSD License

 */
package GroovyWebcam

import javafx.animation.AnimationTimer
import javafx.embed.swing.SwingFXUtils
import javafx.scene.canvas.Canvas
import javafx.scene.image.Image
import org.bytedeco.javacv.Frame
import org.bytedeco.javacv.FrameGrabber
import org.bytedeco.javacv.FrameRecorder
import org.bytedeco.javacv.Java2DFrameConverter

// Maintain the context of recording
class RecordingContext {

    // Recording params
    String _outputFile
    boolean _recording = false
    boolean _paused = false
    int _device = 0

    // JavaCv params
    Java2DFrameConverter _converter
    FrameGrabber _grabber
    Frame _img
    boolean _grabbing
    long _grabTime
    int _videoWidth
    int _videoHeight
    double _deviceFrameRate

    // Preview Stuff
    AnimationTimer _timer
    Image _nextImage
    boolean _previewing = false

    double PreviewWidth
    double PreviewHeight

    private RecordingContext(){
        _converter = new Java2DFrameConverter()
    }

    // Webcam context
    public static RecordingContext WebcamRecordingContext(int device){
        def ctx = new RecordingContext()
        ctx._device = device
        return ctx
    }

    // Screen context (todo)
    public static RecordingContext ScreenRecordingContext(int monitor){
        // TODO (JavaCv doesn't work)
        return null
    }

    // Start grabbing frames
    public void StartGrabber() {

        _grabber =  FrameGrabber.createDefault(_device)
        _grabber.start()
        _grabbing = true

        Thread grabThread = new Thread(new Runnable() {
            @Override
            void run() {
                while (_grabbing) {
                    _img = _grabber.grab()
                    _grabTime = System.currentTimeMillis()
                }

                _grabber.stop()
                _grabber = null
            }
        })

        grabThread.start()
    }

    // Get device params
    public void ProfileDevice(long ms){
        int frames = 0

        // Wait for device
        while (_img == null)
        {
            Thread.sleep(10)
        }

        // Check frame rate of device
        long start_time = System.currentTimeMillis()
        long end_time
        long time_diff = 0
        long lastGrab = _grabTime

        while (time_diff < ms)
        {
            if(_grabTime > lastGrab)
            {
                lastGrab = _grabTime
                frames++
            }

            end_time = System.currentTimeMillis()
            time_diff = end_time - start_time
        }

        _deviceFrameRate = ( frames / time_diff * 1000).doubleValue().round()
        _videoWidth = _grabber.getImageWidth()
        _videoHeight = _grabber.getImageHeight()

    }

    // Stop device
    public void StopGrabber() {
        _grabbing = false
    }

    // Start preview
    public void StartPreview(Canvas preview) {

        if(_grabber == null){
            StartGrabber()
        }

        _previewing = true

        _timer = new AnimationTimer() {
            @Override
            void handle(long now) {
                if(_img != null) {
                    if (!_recording) {
                        def newImg = _converter.convert(_img)
                        _nextImage = SwingFXUtils.toFXImage(newImg, null)
                    }
                    preview.getGraphicsContext2D().drawImage(_nextImage, 0.0, 0.0, PreviewWidth, PreviewHeight)
                }
            }
        }
        _timer.start()
    }

    // Stop the preview
    public void StopPreview() {

        if(!_previewing) return

        _previewing = false
        _timer.stop()
    }

    // Record to video (uses FFMpeg via JavaCv)
    public void StartRecording(String file) {

        _recording = true
        _outputFile = file

        Thread captureTask = new Thread(new Runnable() {
            @Override
            void run() {

                FrameRecorder recorder = FrameRecorder.createDefault(_outputFile, _videoWidth, _videoHeight)
                recorder.setFrameRate(_deviceFrameRate)
                recorder.start()

                long lastGrab = _grabTime

                // Record video
                while (_recording)
                {
                    if(_grabTime > lastGrab){
                        lastGrab = _grabTime
                        def tmp = _img
                        if(!_paused) {
                            recorder.record(tmp)
                        }

                        // Some issue in JavaCv
                        // If preview thread does the convert before image is sent to recorder..
                        // Kernel dump happens..
                        // Guess converts changes the _img altogether
                        if(_previewing){
                            def newImg = _converter.convert(_img)
                            _nextImage = SwingFXUtils.toFXImage(newImg, null)
                        }
                    }
                }

                recorder.stop()
            }
        })

        captureTask.start()
    }

    // Stop recording
    public void StopRecording() {
        _recording = false
    }

    // Pause sending grabbed frames to recorder
    public void PauseRecording() {
        _paused = true
    }

    // Resume
    public void ResumeRecording() {
        _paused = false
    }

    // Are we recording?
    public boolean IsRecording(){
        return  _recording
    }

}

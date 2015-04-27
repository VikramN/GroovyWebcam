# GroovyWebcam

Groovy + JavaFx Webcam Recorder

## License

WebcamGroovy => MIT / GPL

JavaCv https://github.com/bytedeco/javacv => GPL

FFMpeg https://www.ffmpeg.org => GPL / LGPL

OpenCv http://opencv.org/ => 3-clause BSD License

## Depends On

Groovy

JavaCV (https://github.com/bytedeco/javacv)

## Example

```Groovy

    def _context = RecordingContext.WebcamRecordingContext(0)
    _context.StartGrabber()
    _context.ProfileDevice(1000)
    _context.StartPreview(previewCanvas)
    _context.StartRecording("out.mp4")
    _context.StopRecording()
```

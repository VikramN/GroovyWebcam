/*
@author - https://github.com/VikramN/

LICENSE
WebcamGroovy => MIT / GPL
JavaCv https://github.com/bytedeco/javacv => GPL
FFMpeg https://www.ffmpeg.org => GPL / LGPL
OpenCv http://opencv.org/ => 3-clause BSD License

 */
package GroovyWebcam

import groovy.transform.CompileStatic
import javafx.application.Application
import javafx.beans.value.ChangeListener
import javafx.beans.value.ObservableValue
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.canvas.Canvas
import javafx.scene.control.Button
import javafx.scene.layout.Pane
import javafx.stage.Stage

// Main class

@CompileStatic
class Cam extends Application {

    // Launch
    static main(String[] args){
        launch(Cam.class, args)
    }


    // Context
    RecordingContext _context

    // App Start
    @Override
    void start(Stage primaryStage) throws Exception {

        // Default cam for now
        _context = RecordingContext.WebcamRecordingContext(0)

        // No mood for MVC
        // Output goes to out.mp4
        // Change if you want to

        Parent root = FXMLLoader.load(getClass().getResource("sample.fxml")) as Parent
        Scene scene = new Scene(root, primaryStage.getWidth(), primaryStage.getHeight())

        def previewCanvas = scene.lookup("#imgHolder") as Canvas

        def container =  scene.lookup("#container") as Pane

        container.widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                def w = newValue.doubleValue()
                previewCanvas.setWidth(w)
                _context.PreviewWidth = w
            }
        })

        container.heightProperty().addListener(new ChangeListener<Number>(){
            @Override
            void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                def h = newValue.doubleValue()
                previewCanvas.setHeight(h)
                _context.PreviewHeight = h
            }
        })

        def btnRecord = scene.lookup("#btnRecord") as Button
        def btnSave = scene.lookup("#btnSave") as Button

        btnSave.setDisable(true)
        btnRecord.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            void handle(ActionEvent event) {
                _context.StartRecording("out.mp4")
                btnRecord.setDisable(true)
                btnSave.setDisable(false)
            }
        })

        def btnPause = scene.lookup("#btnPause") as Button
        def btnResume = scene.lookup("#btnResume") as Button

        btnPause.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            void handle(ActionEvent event) {
                _context.PauseRecording()
                btnPause.setDisable(true)
                btnResume.setDisable(false)
            }
        })

        btnResume.setDisable(true)
        btnResume.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            void handle(ActionEvent event) {
                _context.ResumeRecording()
                btnResume.setDisable(true)
                btnPause.setDisable(false)
            }
        })

        btnSave.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            void handle(ActionEvent event) {
                _context.StopRecording()
                btnResume.setDisable(true)
                btnPause.setDisable(true)
                btnRecord.setDisable(false)
            }
        })

        primaryStage.setTitle("Recorder")
        primaryStage.setScene(scene)
        primaryStage.show()

        // Get hold of webcam (no exception handling as of now)
        _context.StartGrabber()

        // Check device params for 1 sec
        _context.ProfileDevice(1000)

        // Start  the preview
        _context.StartPreview(previewCanvas)
    }

    // App Close
    @Override
    void stop() {
        if(_context != null){
            _context.StopRecording()
            _context.StopPreview()
            _context.StopGrabber()
            _context = null
        }

        super.stop()
    }

}


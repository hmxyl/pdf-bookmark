package com.local.bookmark;

import com.itextpdf.text.exceptions.BadPasswordException;
import com.local.bookmark.action.PDFContents;
import com.local.bookmark.action.PDFUtil;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.Dragboard;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;


public class Main extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("生成PDF目录");


        TextField filePath = new TextField();
        filePath.setEditable(false);
        filePath.setPromptText("请选择PDF文件");


        Button fileSelectorBtn = new Button("选择文件");


        BorderPane topPane = new BorderPane();
        topPane.setCenter(filePath);
        TextField pageIndexOffset = new TextField();
        topPane.setRight(new HBox(pageIndexOffset, fileSelectorBtn));


        pageIndexOffset.setPromptText("页码偏移量");
        pageIndexOffset.setPrefWidth(100);
        pageIndexOffset.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!observable.getValue()) {
                String offset = pageIndexOffset.getText();
                if (offset != null && offset.length() > 0 && !offset.matches("[0-9]+")) {
                    showDialog("错误", "偏移量设置错误", "页码偏移量只能为整数", Alert.AlertType.ERROR);
                }
            }
        });


        Button contentsGenerator = new Button("生成目录");
        Button getContents = new Button("获取目录");
        BorderPane bottomPane = getBottomPane(contentsGenerator, getContents);


        // 目录文本内容
        TextArea textArea = new TextArea();
        textArea.setPromptText("请在此填入目录内容");
        textArea.setOnDragEntered(e -> {
            Dragboard dragboard = e.getDragboard();
            File file = dragboard.getFiles().get(0); //获取拖入的文件
            String fileName = file.getName();
            if (fileName.matches("[\\s\\S]+.[pP][dD][fF]$")) {
                filePath.setText(file.getPath());
            }
        });
        textArea.textProperty().addListener(event -> {
            if (textArea.getText().trim().startsWith("http")) {
                getContents.setDisable(false);
            } else {
                getContents.setDisable(true);
            }
        });


        // “选择文件”操作
        fileSelectAction(fileSelectorBtn, filePath);
        // “获取目录”操作
        getContentsAction(getContents, textArea);
        // “生成目录”操作
        generatorContentsAction(filePath, pageIndexOffset, contentsGenerator, textArea);


        BorderPane vBox = new BorderPane();
        vBox.setTop(topPane);
        vBox.setCenter(textArea);
        vBox.setBottom(bottomPane);
        vBox.setPadding(new Insets(10, 10, 10, 10));
        Scene scene = new Scene(vBox, 600, 400);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void generatorContentsAction(TextField filePath, TextField pageIndexOffset, Button contentsGenerator, TextArea textArea) {
        contentsGenerator.setOnAction(event -> {
            // 文件路径
            String fp = filePath.getText();
            if (fp == null || fp.isEmpty()) {
                showDialog("错误", "pdf文件路径为空", "pdf文件路径不能为空，请选择pdf文件", Alert.AlertType.ERROR);
                return;
            }
            String srcFile = fp.replaceAll("\\\\", "/");
            String srcFileName = srcFile.substring(srcFile.lastIndexOf("/") + 1);
            String ext = srcFileName.substring(srcFileName.lastIndexOf("."));
            String destFile = srcFile.substring(0, srcFile.lastIndexOf(srcFileName)) + srcFileName.substring(0, srcFileName.lastIndexOf(".")) + "_含目录" + ext;

            String offset = pageIndexOffset.getText();
            String content = textArea.getText();
            if (content != null && !content.isEmpty()) {
                try {
                    PDFUtil.addBookmark(textArea.getText(), srcFile, destFile, Integer.parseInt(offset != null && !offset.isEmpty() ? offset : "0"));
                } catch (Exception e) {
                    String errInfo = e.toString();
                    if (e.getCause().getClass() == BadPasswordException.class) {
                        errInfo = "PDF已加密，无法完成修改";
                    }
                    showDialog("错误", "添加目录错误", errInfo, Alert.AlertType.INFORMATION);
                    return;
                }
                showDialog("通知", "添加目录成功！", "文件存储在" + destFile, Alert.AlertType.INFORMATION);
            } else {
                showDialog("错误", "目录内容为空", "目录能容不能为空,请填写pdf书籍目录url或者填入目录文本", Alert.AlertType.ERROR);
            }
        });
    }

    private static void getContentsAction(Button getContents, TextArea textArea) {
        getContents.setOnAction(event -> {
            //  Jsoup 获取目录
            String contents = PDFContents.getContentsByUrl(textArea.getText());
            textArea.setText(contents);
        });
    }

    private static void fileSelectAction(Button fileSelectorBtn, TextField filePath) {
        fileSelectorBtn.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("pdf", "*.pdf"));
            File file = fileChooser.showOpenDialog(null);
            if (file != null) {
                filePath.setText(file.getPath());
            }
        });
    }

    private void showDialog(String title, String header, String content, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setContentText(content);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.show();
    }

    private BorderPane getBottomPane(Button contentsGenerator, Button getContents) {
        BorderPane bottomPane = new BorderPane();
        getContents.setDisable(true);
        HBox h = new HBox(20, getContents, contentsGenerator);
        h.setAlignment(Pos.CENTER);
        bottomPane.setCenter(h);
        return bottomPane;
    }
}

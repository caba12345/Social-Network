package com.example.laboratorjavafx.controllers;

import com.example.laboratorjavafx.HelloApplication;
import com.example.laboratorjavafx.domain.User;
import com.example.laboratorjavafx.domain.validators.UserValidator;
import com.example.laboratorjavafx.domain.validators.ValidationException;
import com.example.laboratorjavafx.service.MessageService;
import com.example.laboratorjavafx.service.Service;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;

public class SignUp {

    @FXML
    private TextField first_name;
    @FXML
    private TextField last_name;
    @FXML
    private TextField email;
    @FXML
    private TextField password;
    @FXML
    private TextField password_confirm;

    @FXML
    private Text firstnameErrorText;
    @FXML
    private Text lastnameErrorText;
    @FXML
    private Text emailErrorText;
    @FXML
    private Text passwordErrorText;

    private UserValidator userValidator = new UserValidator();
    private Service service;

    private MessageService messageService;
    public void setService(Service service) {
        this.service = service;
    }
    public Service getService() {
        return service;
    }

    public MessageService getMessageService() {
        return messageService;
    }

    public void setMessageService(MessageService messageService) {
        this.messageService = messageService;
    }

    @FXML
    protected void onCreateAccountClick(ActionEvent event) throws IOException {
        User newUser = new User(first_name.getText(), last_name.getText(), email.getText(), password.getText());

        try{
            userValidator.validate(newUser);
        }
        catch (ValidationException exception) {
            String err = exception.toString().split(":")[1];
            switch (err.charAt(1)) {
                case '1' -> {
                    firstnameErrorText.setText(err.substring(1));
                    firstnameErrorText.setVisible(true);
                }
                case '2' -> {
                    lastnameErrorText.setText(err.substring(1));
                    lastnameErrorText.setVisible(true);
                }
                default -> {
                    emailErrorText.setText(err);
                    System.out.println(err);
                    emailErrorText.setVisible(true);
                }
            }
            return;
        }

        if(!password.getText().equals(password_confirm.getText()))
            passwordErrorText.setVisible(true);
        else { // adaugam utilizator
            service.addUser(newUser);

            FXMLLoader stageLoader = new FXMLLoader(HelloApplication.class.getResource("UserInterface.fxml"));
            Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();

            AnchorPane appLayout = stageLoader.load();
            Scene scene = new Scene(appLayout);
            stage.setScene(scene);

            UserController appController = stageLoader.getController();
            appController.setService(this.service);
            appController.initApp(newUser);
            appController.setMessageService(messageService);

            stage.show();

            System.out.println("yay!");
        }
    }

    public void goBackToLogIn(ActionEvent actionEvent) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("LogIn.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        Stage stage = (Stage)((Node)actionEvent.getSource()).getScene().getWindow();
        stage.setScene(scene);

        LogIn logInController = fxmlLoader.getController();
        logInController.setService(this.service);
        logInController.setMessageService(messageService);
        stage.show();
    }

    public void onFirstnameTextChanged() {
        firstnameErrorText.setVisible(false);
    }

    public void onLastnameTextChanged() {
        lastnameErrorText.setVisible(false);
    }

    public void onEmailTextChanged() {
        emailErrorText.setVisible(false);
    }

    public void onPasswordTextChanged() {
        passwordErrorText.setVisible(false);
    }

    public void onConfirmPasswordTextChanged() {
        passwordErrorText.setVisible(false);
    }
}

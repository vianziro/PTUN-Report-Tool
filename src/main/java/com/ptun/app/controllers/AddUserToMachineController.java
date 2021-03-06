package com.ptun.app.controllers;

import com.j256.ormlite.dao.Dao;
import com.ptun.app.apis.endpoints.EasyLinkPoints;
import com.ptun.app.customui.NumberTextField;
import com.ptun.app.db.models.User;
import com.ptun.app.enums.PEGAWAI_CHOICES;
import com.ptun.app.eventbus.EventBus;
import com.ptun.app.eventbus.events.NewUserEvent;
import com.ptun.app.statics.Constants;
import com.ptun.app.statics.Util;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import lombok.Data;
import tray.notification.NotificationType;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

/**
 * Created by khairulimam on 01/05/17.
 */
@Data
public class AddUserToMachineController implements Initializable {

    @FXML
    private TextField tfNama;
    @FXML
    private NumberTextField tfPin;
    @FXML
    private RadioButton rbHakim, rbStaff, rbHonorer;
    @FXML
    private Button btnSimpan;

    private ToggleGroup rbJabatanGroup = new ToggleGroup();
    private Dao<User, Integer> dao = User.getDao();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        rbHakim.setText(PEGAWAI_CHOICES.HAKIM.name());
        rbStaff.setText(PEGAWAI_CHOICES.PEGAWAI.name());
        rbHonorer.setText(PEGAWAI_CHOICES.HONORER.name());
        rbHakim.setToggleGroup(rbJabatanGroup);
        rbStaff.setToggleGroup(rbJabatanGroup);
        rbHonorer.setToggleGroup(rbJabatanGroup);
    }

    private RadioButton getPegawai() {
        return (RadioButton) rbJabatanGroup.getSelectedToggle();
    }

    @FXML
    private void addUser(ActionEvent actionEvent) {
        btnSimpan.setDisable(true);
        btnSimpan.setText("Menyimpan ...");
        try {
            int PIN = Integer.parseInt(tfPin.getText());
            User newUser = new User();
            newUser.setNama(tfNama.getText());
            newUser.setPIN(PIN);
            newUser.setJabatan(getPegawai().getText());
            dao.create(newUser);
            boolean result = EasyLinkPoints.getClient().addNewUser(Constants.SN, newUser.getPIN(), tfNama.getText(), 0, 0, 0, Util.getTemplateForPin(newUser.getPIN())).execute().body().isResult();
            if (result) {
                String message = String.format("%s baru berhasil ditambahkan. Silahkan tambahkan sidik jari baru untuk user:" +
                        "\nNama \t: %s" +
                        "\nPIN \t\t: %d" +
                        "\n\nSelalu ingat untuk menambahkan sidik jari untuk user yang telah didaftarkan!", newUser.getJabatan(), getTfNama().getText(), newUser.getPIN());
                Alert alert = Util.setUpDialog("Sukses", "Berhasil menambahkan user baru", message, Alert.AlertType.INFORMATION);
                alert.showAndWait();
                resetForm();
                EventBus.getDefault().post(new NewUserEvent(newUser));
            } else {
                dao.delete(newUser);
                Util.showNotif("Error", "User gagal ditambahkan. Kemungkinan PIN yang dimasukkan sudah digunakan!", NotificationType.ERROR);
            }
        } catch (NullPointerException e) {
            getBack();
            Util.showNotif("Error", String.format("Ada kesalahan %s", e.getMessage()), NotificationType.ERROR);
            e.printStackTrace();
        } catch (IOException e) {
            getBack();
            e.printStackTrace();
        } catch (SQLException e) {
            getBack();
            e.printStackTrace();
        } catch (NumberFormatException e) {
            getBack();
            Util.showNotif("Error", String.format("Ada kesalahan %s", e.getMessage()), NotificationType.ERROR);
            e.printStackTrace();
        }
        getBack();
    }

    private void getBack() {
        btnSimpan.setText("Simpan");
        btnSimpan.setDisable(false);
    }

    public void resetForm() {
        tfNama.setText("");
        tfPin.setText("");
        getRbJabatanGroup().getSelectedToggle().setSelected(false);
    }

}

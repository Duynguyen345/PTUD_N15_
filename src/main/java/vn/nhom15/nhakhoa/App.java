package vn.nhom15.nhakhoa;

import java.io.IOException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Modality;
import javafx.stage.Stage;
import vn.nhom15.nhakhoa.connect.ConnectDB;
import vn.nhom15.nhakhoa.model.SessionContext;

/** Entry point + dieu huong (setRoot/openModal) + phien dang nhap (RULES muc 8). Chu file: Duy. */
public class App extends Application {

    public static SessionContext session;
    private static Stage primaryStage;
    private static Scene scene;

    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        try {
            ConnectDB.getInstance().connect();
        } catch (RuntimeException ex) {
            Alert a = new Alert(Alert.AlertType.ERROR,
                    "Không thể kết nối SQL Server. Kiểm tra db.properties và DATA/README.md.\nChi tiết: "
                            + ex.getMessage(),
                    ButtonType.OK);
            a.setHeaderText("Lỗi kết nối CSDL");
            a.showAndWait();
            return;
        }
        setRoot("GD_DangNhap");
        primaryStage.setTitle("Phòng khám Nha khoa Sài Gòn - DentistClinic (Nhóm 15)");
        primaryStage.show();
    }

    /** Doi man hinh chinh cua cua so chinh. */
    public static void setRoot(String tenFxml) {
        try {
            Parent root = FXMLLoader.load(App.class.getResource("/fxml/" + tenFxml + ".fxml"));
            if (scene == null) {
                scene = new Scene(root);
            } else {
                scene.setRoot(root);
            }
            primaryStage.setScene(scene);
            primaryStage.centerOnScreen();
        } catch (IOException ex) {
            throw new RuntimeException("Không mở được màn hình " + tenFxml + ": " + ex.getMessage(), ex);
        }
    }

    /** Mo man hinh modal (showAndWait) - kieu N11. */
    public static void openModal(String tenFxml) {
        openModal(tenFxml, 0, 0);
    }

    public static void openModal(String tenFxml, double width, double height) {
        try {
            Parent root = FXMLLoader.load(App.class.getResource("/fxml/" + tenFxml + ".fxml"));
            Stage modal = new Stage();
            modal.initOwner(primaryStage);
            modal.initModality(Modality.WINDOW_MODAL);
            modal.setTitle(tenFxml);
            modal.setScene(new Scene(root));
            if (width > 0) {
                modal.setWidth(width);
            }
            if (height > 0) {
                modal.setHeight(height);
            }
            modal.showAndWait();
        } catch (IOException ex) {
            throw new RuntimeException("Không mở được màn hình " + tenFxml + ": " + ex.getMessage(), ex);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}

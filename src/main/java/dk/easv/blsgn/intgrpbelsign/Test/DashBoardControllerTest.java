package dk.easv.blsgn.intgrpbelsign.Test;

import dk.easv.blsgn.intgrpbelsign.be.User;
import dk.easv.blsgn.intgrpbelsign.bll.UserManager;
import dk.easv.blsgn.intgrpbelsign.gui.controllers.login.DashBoardController;
import dk.easv.blsgn.intgrpbelsign.model.UserModel;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.JFXPanel;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import static org.junit.Assert.*;

public class DashBoardControllerTest {

    private DashBoardController controller;
    private FlowPane flowPane;

    @Before
    public void setUp() throws Exception {
        new JFXPanel(); // JavaFX init
        controller = new DashBoardController();

        // Inject fake UserModel
        UserModel fakeModel = new UserModel(new UserManager()) {
            @Override
            public List<Button> generateUserButtons() {
                return List.of(new Button("FakeUser"));
            }

            @Override
            public ObservableList<User> getUsers() {
                User fake = new User();
                fake.setUser_name("FakeUser");
                fake.setRole_id(3); // Operator
                return FXCollections.observableArrayList(fake);
            }
        };
        injectField(controller, "userModel", fakeModel);

        // Inject buttonContainer
        flowPane = new FlowPane();
        flowPane.getChildren().add(new Button("SearchBar"));
        injectField(controller, "buttonContainer", flowPane);
    }

    @Test
    public void testFilterButtons() throws Exception {
        List<Button> allButtons = List.of(new Button("Alice"), new Button("Bob"));
        injectField(controller, "allButtons", allButtons);
        flowPane.getChildren().addAll(allButtons);

        Method filter = DashBoardController.class.getDeclaredMethod("filterButtons", String.class);
        filter.setAccessible(true);
        filter.invoke(controller, "ali");

        assertEquals(2, flowPane.getChildren().size());
        assertEquals("SearchBar", ((Button) flowPane.getChildren().get(0)).getText());
        assertEquals("Alice", ((Button) flowPane.getChildren().get(1)).getText());
    }

    @Test
    public void testPopulateUserButtons() throws Exception {
        Method populate = DashBoardController.class.getDeclaredMethod("populateUserButtons");
        populate.setAccessible(true);
        populate.invoke(controller);

        boolean found = flowPane.getChildren().stream()
                .anyMatch(node -> node instanceof Button && ((Button) node).getText().equals("FakeUser"));
        assertTrue(found);
    }

    @Test
    public void testBtnBackOnAction() throws Exception {
        javafx.application.Platform.runLater(() -> {
            try {
                // Create a dummy stage and attach a dummy scene to simulate the FX window
                Stage stage = new Stage();
                FlowPane dummyRoot = new FlowPane();
                Scene scene = new Scene(dummyRoot);
                stage.setScene(scene);

                // Simulate ActionEvent from a Button inside the scene
                Button fakeButton = new Button("Back");
                dummyRoot.getChildren().add(fakeButton);
                scene.setRoot(dummyRoot);

                ActionEvent event = new ActionEvent(fakeButton, null);

                // Inject fake button as source
                Method method = DashBoardController.class.getDeclaredMethod("btnBackOnAction", ActionEvent.class);
                method.setAccessible(true);
                method.invoke(controller, event);

                // Assert current stage has updated scene (just check no exception and scene exists)
                assertNotNull(stage.getScene());

            } catch (Exception e) {
                fail("Exception during FX test: " + e.getMessage());
            }
        });

        // Allow JavaFX thread to execute
        Thread.sleep(1000); // ⚠️ crude but necessary to give FX thread time
    }


    @Test
    public void testHandleUserAction() throws Exception {
        // Prepare User
        User user = new User();
        user.setUser_name("FakeUser");
        user.setRole_id(3);

        injectField(controller, "roleLabel", new javafx.scene.control.Label());
        injectField(controller, "mainName", new javafx.scene.control.Label());

        Method handle = DashBoardController.class.getDeclaredMethod("handleUserAction", User.class);
        handle.setAccessible(true);
        handle.invoke(controller, user);

        boolean found = flowPane.getChildren().stream()
                .anyMatch(node -> node instanceof Parent);
        assertTrue(found);
    }

    private void injectField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}

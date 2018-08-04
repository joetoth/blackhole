import com.google.gson.Gson;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Blackhole extends AnAction {
    @Override
    public void actionPerformed(AnActionEvent event) {
        Project project = event.getData(PlatformDataKeys.PROJECT);
        final Editor editor = event.getData(PlatformDataKeys.EDITOR);
        final VirtualFile file = event.getData(PlatformDataKeys.VIRTUAL_FILE);

        if (!editor.getSelectionModel().hasSelection()) {
            editor.getSelectionModel().selectLineAtCaret();
        }
        String selected = editor.getSelectionModel().getSelectedText();
        editor.getSelectionModel().removeSelection();

        Integer lineno = (editor != null)
                // convert the VisualPosition to the LogicalPosition to have the correct line number.
                // http://grepcode.com/file/repository.grepcode.com/java/ext/com.jetbrains/intellij-idea/10.0/com/intellij/openapi/editor/LogicalPosition.java#LogicalPosition
                ? editor.visualToLogicalPosition(
                editor.getSelectionModel().getSelectionStartPosition()).line + 1 : null;

        InputEvent inputEvent = event.getInputEvent();
        Map inputEventJson = new HashMap();
        inputEventJson.put("is_alt_down", inputEvent.isAltDown());
        inputEventJson.put("is_alt_graph_down", inputEvent.isAltGraphDown());
        inputEventJson.put("is_control_down", inputEvent.isControlDown());
        inputEventJson.put("is_meta_down", inputEvent.isMetaDown());
        inputEventJson.put("is_shift_down", inputEvent.isShiftDown());
        if (inputEvent instanceof KeyEvent) {
            KeyEvent keyEvent = (KeyEvent) inputEvent;
            inputEventJson.put("type", "keyboard");
            inputEventJson.put("key_char", keyEvent.getKeyChar());
            inputEventJson.put("key_code", keyEvent.getKeyCode());
        } else if(inputEvent instanceof MouseEvent) {
            MouseEvent mouseEvent = (MouseEvent) inputEvent;
            inputEventJson.put("type", "mouse");
            inputEventJson.put("button", mouseEvent.getButton());
            inputEventJson.put("click_count", mouseEvent.getClickCount());
            inputEventJson.put("x", mouseEvent.getX());
            inputEventJson.put("y", mouseEvent.getY());
        }

        Gson g = new Gson();
        Map json = new HashMap();
        json.put("context", "intellij");
        json.put("file_path", file.getPath());
        json.put("project_path", project.getBaseDir().getPath());
        json.put("lineno", lineno);
        json.put("text", selected);
        json.put("event", inputEventJson);
        String[] cmd = {"/usr/local/bin/blackhole", g.toJson(json)};
        try {
            System.out.println(cmd);
            Runtime.getRuntime().exec(cmd);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}


import com.google.gson.Gson;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

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

        Map json = new HashMap();
        json.put("context", "intellij");
        json.put("file_path", file.getPath());
        json.put("project_path", project.getBaseDir().getPath());
        json.put("lineno", lineno);
        json.put("text", selected);
        Gson g = new Gson();
        String[] cmd = {"/usr/local/bin/blackhole", g.toJson(json)};
        try {
            Runtime.getRuntime().exec(cmd);
        } catch(IOException e) {
            e.printStackTrace();
        }
    }
}


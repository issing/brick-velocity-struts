package net.isger.brick.velocity.struts;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

import javax.servlet.ServletContext;

import net.isger.util.Strings;
import net.isger.velocity.VelocityConstants;
import net.isger.velocity.directive.DirectiveLibrary;

import com.opensymphony.xwork2.inject.Container;

public class VelocityManager extends
        org.apache.struts2.views.velocity.VelocityManager implements
        VelocityConstants {

    private static final String KEY_FILE_PATH = "strutsfile.resource.loader.path";

    private static final String KEY_DIRECTIVE = "userdirective";

    private List<DirectiveLibrary> libraries;

    public void setContainer(Container container) {
        super.setContainer(container);
        List<DirectiveLibrary> list = new ArrayList<DirectiveLibrary>();
        Set<String> prefixes = container
                .getInstanceNames(DirectiveLibrary.class);
        for (String prefix : prefixes) {
            list.add(container.getInstance(DirectiveLibrary.class, prefix));
        }
        this.libraries = Collections.unmodifiableList(list);
    }

    public Properties loadConfiguration(ServletContext context) {
        // 模板加载路径集
        Properties props = super.loadConfiguration(context);
        String paths = props.getProperty(KEY_FILE_PATH);
        if (paths != null) {
            props.setProperty(KEY_FILE_PATH, transPath(context, paths));
        }
        // 自定义砖头指令集
        StringBuffer buffer = new StringBuffer(512);
        for (DirectiveLibrary library : this.libraries) {
            List<Class<?>> directives = library.getDirectiveClasses();
            if (directives != null) {
                for (Class<?> d : directives) {
                    append(buffer, d.getName());
                }
            }
        }
        String directive = props.getProperty(KEY_DIRECTIVE);
        if (Strings.isEmpty(directive)) {
            buffer.setLength(buffer.length() - 2);
        } else {
            buffer.append(directive);
        }
        props.setProperty(KEY_DIRECTIVE, buffer.toString());
        // 初始默认配置
        initProperty(props, KEY_LAYOUT_PATH, LAYOUT_PATH);
        initProperty(props, KEY_LAYOUT_NAME, LAYOUT_NAME);
        initProperty(props, KEY_THEME_NAME, THEME_NAME);
        initProperty(props, KEY_WIDGET_PATH, WIDGET_PATH);
        return props;
    }

    private void initProperty(Properties props, String key, String def) {
        String value = props.getProperty(key);
        if (Strings.isEmpty(value)) {
            props.setProperty(key, def);
        }
    }

    private String transPath(ServletContext context, String paths) {
        StringBuffer buffer = new StringBuffer(512);
        StringTokenizer st = new StringTokenizer(paths, ",");
        String path;
        while (st.hasMoreTokens()) {
            path = st.nextToken();
            if (!new File(path).isAbsolute()) {
                append(buffer, context.getRealPath(path));
            }
        }
        return buffer.toString();
    }

    private void append(StringBuffer buffer, String value) {
        buffer.append(value).append(", ");
    }

}

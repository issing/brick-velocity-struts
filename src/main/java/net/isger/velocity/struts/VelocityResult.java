package net.isger.velocity.struts;

import java.io.StringWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.isger.util.Strings;
import net.isger.velocity.ContextSecretary;
import net.isger.velocity.VelocityConstants;
import net.isger.velocity.VelocityContext;
import net.isger.velocity.bean.LayoutBean;
import net.isger.velocity.bean.ThemeBean;

import org.apache.struts2.views.velocity.VelocityManager;
import org.apache.velocity.Template;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.context.Context;

import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.util.ValueStack;

public class VelocityResult extends
        org.apache.struts2.dispatcher.VelocityResult implements
        VelocityConstants {

    private static final long serialVersionUID = -3630891950185638015L;

    private ActionInvocation invocation;

    private Template template;

    private String velocityIndex;

    private String velocitySuffix;

    private String contentType;

    private String target;

    private String themePath;

    private String themeName;

    private String layoutSupport;

    private String layoutPath;

    private String layoutName;

    private String layoutCarry;

    private ThemeBean theme;

    private LayoutBean layout;

    public void doExecute(String finalLocation, ActionInvocation invocation)
            throws Exception {
        this.invocation = invocation;
        super.doExecute(finalLocation, invocation);
    }

    protected String getContentType(String templateLocation) {
        if (contentType == null) {
            contentType = super.getContentType(templateLocation);
        } else {
            contentType = conditionalParse(contentType, invocation);
        }
        return contentType;
    }

    protected Template getTemplate(ValueStack stack, VelocityEngine velocity,
            ActionInvocation invocation, String location, String encoding)
            throws Exception {
        this.invocation = invocation;
        String velocitySuffix = getProperty(stack, velocity,
                KEY_VELOCITY_SUFFIX, this.velocitySuffix, VELOCITY_SUFFIX);
        String target = conditionalParse(this.target, invocation);
        if (Strings.isNotEmpty(target)) {
            location += "/" + target;
        } else if (location.endsWith("/")) {
            location += getProperty(stack, velocity, KEY_VELOCITY_INDEX,
                    this.velocityIndex, VELOCITY_INDEX);
        }
        // 获取主题信息
        this.theme = new ThemeBean();
        this.theme.setPath(getProperty(stack, velocity, KEY_THEME_PATH,
                this.themePath, THEME_PATH));
        this.theme.setName(getProperty(stack, velocity, KEY_THEME_NAME,
                this.themeName, THEME_NAME));
        this.theme.setLocation(location);
        // 检查布局信息
        this.layout = new LayoutBean();
        this.layout.setSupport(Boolean.parseBoolean(getProperty(stack,
                velocity, KEY_LAYOUT_SUPPORT, this.layoutSupport,
                LAYOUT_SUPPORT)));
        this.layout.setPath(getProperty(stack, velocity, KEY_LAYOUT_PATH,
                this.layoutPath, LAYOUT_PATH));
        this.layout.setName(getProperty(stack, velocity, KEY_LAYOUT_NAME,
                this.layoutName, LAYOUT_NAME));
        this.layout.setCarry(Boolean.parseBoolean(getProperty(stack, velocity,
                KEY_LAYOUT_CARRY, this.layoutCarry, LAYOUT_CARRY)));
        if (this.layout.isSupport()) {
            // 提取内容页面
            if (this.layout.isCarry()) {
                this.template = super.getTemplate(stack, velocity, invocation,
                        this.theme.getLocation() + velocitySuffix, encoding);
            }
            // 获取布局参数
            StringBuffer buffer = new StringBuffer(128);
            buffer.append(this.layout.getPath());
            buffer.append("/").append(this.theme.getName());
            buffer.append("/").append(this.layout.getName());
            location = buffer.toString();
        } else {
            location = this.theme.getLocation();
        }
        // 提取目标模板（布局/内容）
        return super.getTemplate(stack, velocity, invocation, location
                + velocitySuffix, encoding);
    }

    protected Context createContext(VelocityManager velocityManager,
            ValueStack stack, HttpServletRequest request,
            HttpServletResponse response, String location) {
        VelocityContext context = new VelocityContext(
                velocityManager.getVelocityEngine(), super.createContext(
                        velocityManager, stack, request, response, location));
        ContextSecretary secretary = context.getSecretary();
        secretary.setTheme(theme);
        secretary.setLayout(layout);
        if (this.template != null) {
            // 生成布局模板内容
            StringWriter sw = new StringWriter();
            try {
                this.template.merge(context, sw);
            } catch (Exception e) {
                throw new IllegalStateException(e.getMessage(), e.getCause());
            }
            layout.setScreen(sw.toString());
        }
        return context;
    }

    public void setVelocityIndex(String velocityIndex) {
        this.velocityIndex = velocityIndex;
    }

    public void setVelocitySuffix(String velocitySuffix) {
        this.velocitySuffix = velocitySuffix;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    /**
     * 主题属性
     * 
     * @return
     */
    public void setThemePath(String themePath) {
        this.themePath = themePath;
    }

    public void setThemeName(String themeName) {
        this.themeName = themeName;
    }

    /**
     * 布局属性
     * 
     * @return
     */
    public void setLayoutSupport(String layoutSupport) {
        this.layoutSupport = layoutSupport;
    }

    public void setLayoutPath(String layoutPath) {
        this.layoutPath = layoutPath;
    }

    public void setLayoutName(String layoutName) {
        this.layoutName = layoutName;
    }

    public void setLayoutCarry(String layoutCarry) {
        this.layoutCarry = layoutCarry;
    }

    /**
     * 获取配置属性
     * 
     * @param stack
     * @param velocity
     * @param invocation
     * @param key
     * @param local
     * @param def
     * @return
     */
    private String getProperty(ValueStack stack, VelocityEngine velocity,
            String key, String local, String def) {
        String result = stack.findString(key);
        if (Strings.isNotEmpty(result)) {
            return result;
        }
        result = conditionalParse(local, invocation);
        if (Strings.isNotEmpty(result)) {
            return result;
        }
        Object value = velocity.getProperty(key);
        if (value != null && Strings.isNotEmpty(local = value.toString())) {
            def = local;
        }
        return def;
    }

}

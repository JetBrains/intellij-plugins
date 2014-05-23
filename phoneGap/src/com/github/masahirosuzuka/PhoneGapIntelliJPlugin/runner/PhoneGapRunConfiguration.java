package com.github.masahirosuzuka.PhoneGapIntelliJPlugin.runner;

import com.github.masahirosuzuka.PhoneGapIntelliJPlugin.ui.PhoneGapConfigurationEditor;
import com.github.masahirosuzuka.PhoneGapIntelliJPlugin.util.PhoneGapSettings;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.JavaRunConfigurationExtensionManager;
import com.intellij.execution.configurations.*;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.DefaultJDOMExternalizer;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.openapi.util.text.StringUtil;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.github.masahirosuzuka.PhoneGapIntelliJPlugin.util.PhoneGapSettings.*;

/**
 * PhoneGapRunConfiguration.java
 * <p/>
 * Created by Masahiro Suzuka on 2014/04/05.
 */
@SuppressWarnings("UnusedDeclaration")
public class PhoneGapRunConfiguration extends LocatableConfigurationBase {
    public enum Type {
        CORDOVA {
            @Override
            public String getPath() {
                return PhoneGapSettings.getInstance().getCordovaExecutablePath();
            }

            @Override
            public String getName() {
                return "cordova";
            }
        },
        PHONEGAP {
            @Override
            public String getPath() {
                return PhoneGapSettings.getInstance().getPhoneGapExecutablePath();
            }

            @Override
            public String getName() {
                return "phonegap";
            }
        };

        public abstract String getPath();

        public abstract String getName();
    }

    @Nullable
    public String getExecutableTypeCode() {
        if (myExecutableType != null) {
            return myExecutableType.getName();
        }

        return null;
    }

    public void setExecutableTypeByCode(@Nullable String executableTypeCode) {
        for (Type type : Type.values()) {
            if (type.getName().equals(executableTypeCode)) {
                this.myExecutableType = type;
            }
        }
    }

    @Nullable
    private Type myExecutableType;
    @Nullable
    public String myExecutableTypeCode;
    @Nullable
    public String myCommand;
    @Nullable
    public String myPlatform;

    @Nullable
    public Type getExecutableType() {
        if (myExecutableType == null) {
            setExecutableTypeByCode(myExecutableTypeCode);
        }
        return myExecutableType;
    }

    public void setExecutableType(@Nullable Type myExecutableType) {
        this.myExecutableType = myExecutableType;
        this.myExecutableTypeCode = myExecutableType != null ? myExecutableType.getName() : null;
    }

    @Nullable
    public String getCommand() {
        return myCommand;
    }

    public void setCommand(@Nullable String myCommand) {
        this.myCommand = myCommand;
    }

    @Nullable
    public String getPlatform() {
        return myPlatform;
    }

    public void setPlatform(@Nullable String myPlatform) {
        this.myPlatform = myPlatform;
    }


    public PhoneGapRunConfiguration(Project project, ConfigurationFactory factory, String name) {
        super(project, factory, name);

        //defaults
    }

    @Override
    public String suggestedName() {
        return "Run phonegap";
    }

    @Override
    public void readExternal(Element element) throws InvalidDataException {
        super.readExternal(element);
        JavaRunConfigurationExtensionManager.getInstance().readExternal(this, element);

        DefaultJDOMExternalizer.readExternal(this, element);
    }

    @Override
    public void writeExternal(Element element) throws WriteExternalException {
        super.writeExternal(element);
        JavaRunConfigurationExtensionManager.getInstance().writeExternal(this, element);

        DefaultJDOMExternalizer.writeExternal(this, element);
    }

    @NotNull
    @Override
    public SettingsEditor<? extends RunConfiguration> getConfigurationEditor() {
        return new PhoneGapConfigurationEditor(getProject());
    }

    @Override
    public void checkConfiguration() throws RuntimeConfigurationException {
        PhoneGapSettings settings = PhoneGapSettings.getInstance();
        if (myExecutableType == Type.CORDOVA && !settings.isCordovaAvailable()
                || myExecutableType == Type.PHONEGAP && !settings.isPhoneGapAvailable()) {
            throw new RuntimeConfigurationException("SDK is missing");
        }

        if (StringUtil.isEmpty(myCommand)) {
            throw new RuntimeConfigurationException("Command is missing");
        }

        if (!(PHONEGAP_PLATFORM_ANDROID.equals(myPlatform) ||
                PHONEGAP_PLATFORM_IOS.equals(myPlatform) ||
                PHONEGAP_PLATFORM_WP.equals(myPlatform) ||
                PHONEGAP_PLATFORM_RIPPLE.equals(myPlatform))) {
            throw new RuntimeConfigurationException("Platform is missing");
        }
    }

    @SuppressWarnings("CloneDoesntCallSuperClone")
    @Override
    public PhoneGapRunConfiguration clone() {
        final Element element = new Element("toClone");
        try {
            writeExternal(element);
            PhoneGapRunConfiguration configuration =
                    (PhoneGapRunConfiguration) getFactory().createTemplateConfiguration(getProject());
            configuration.setName(getName());
            configuration.readExternal(element);
            return configuration;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Nullable
    @Override
    public RunProfileState getState(@NotNull Executor executor,
                                    @NotNull ExecutionEnvironment executionEnvironment) throws ExecutionException {

        return new PhoneGapRunProfileState(getProject(), executionEnvironment, this);
    }
}

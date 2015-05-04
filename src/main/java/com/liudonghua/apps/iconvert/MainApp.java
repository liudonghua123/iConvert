package com.liudonghua.apps.iconvert;

import java.io.File;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

import javafx.application.Application;
import javafx.stage.Stage;

import com.liudonghua.apps.iconvert.utils.Utils;
import com.liudonghua.apps.iconvert.view.MainPaneController;

public class MainApp extends Application {

    private Locale locale;
    private ResourceBundle i18nBundle;
    private Stage primaryStage;
    private String resourceBaseName = "ApplicationResources";
    public Map<String, String> themes;
    public Map<String, Locale> locales;

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;
        initialize();
        showMainPane();
    }

    public void initialize() {
        String rootPath = new File(getClass().getProtectionDomain().getCodeSource().getLocation()
                .getPath()).getParent();
        locale = getLocale();
        i18nBundle = Utils.loadResourceBundle(rootPath, resourceBaseName, locale);
        locales = Utils.setupLocales(rootPath, resourceBaseName);
        themes = Utils.setupThemes(rootPath);
    }

    public void showMainPane() {
        MainPaneController controller = new MainPaneController(this);
        controller.show();
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    public ResourceBundle getI18nBundle() {
        return i18nBundle;
    }

    public void setI18nBundle() {
        i18nBundle = ResourceBundle.getBundle(
                "com.liudonghua.apps.signapk_fx.ApplicationResources", locale);
    }

    public Locale getLocale() {
        Preferences prefs = Preferences.userNodeForPackage(MainApp.class);
        String lang = prefs.get("locale_lang", null);
        if (lang == null) {
            return Locale.getDefault();
        }
        String country = prefs.get("locale_country", "");
        return new Locale(lang, country);
    }

    public void setLocale(Locale locale) {
        Preferences prefs = Preferences.userNodeForPackage(MainApp.class);
        prefs.put("locale_lang", locale.getLanguage());
        prefs.put("locale_country", locale.getCountry());
    }

    public Locale getCurrentInUseLocale() {
        return locale;
    }

    public void setCurrentInUseLocale(Locale locale) {
        this.locale = locale;
    }

    public String getTheme() {
        Preferences prefs = Preferences.userNodeForPackage(MainApp.class);
        String theme = prefs.get("theme", null);
        if (theme == null) {
            theme = themes.get("default");
        }
        return theme;
    }

    public void setTheme(String theme) {
        Preferences prefs = Preferences.userNodeForPackage(MainApp.class);
        prefs.put("theme", theme);
    }

    public static void main(String[] args) {
        launch(MainApp.class, args);
    }

}

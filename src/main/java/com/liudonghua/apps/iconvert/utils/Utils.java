package com.liudonghua.apps.iconvert.utils;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.environment.EnvironmentUtils;

public class Utils {

    public static ResourceBundle loadResourceBundle(String rootPath, String resourceBaseName,
            Locale locale) {
        File file = new File(rootPath, "resources/locales");
        ClassLoader loader = null;
        try {
            URL[] urls = { file.toURI().toURL() };
            loader = new URLClassLoader(urls);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return ResourceBundle.getBundle(resourceBaseName, locale, loader);
    }

    public static Map<String, Locale> setupLocales(String rootPath, String resourceBaseName) {
        Map<String, Locale> locales = new TreeMap<>();
        File themeDir = new File(rootPath, "resources/locales/");
        FileFilter fileFilter = new FileFilter() {
            @Override
            public boolean accept(File file) {
                String fileName = file.getName();
                if (fileName.length() == 0 || fileName.lastIndexOf(".") == -1) {
                    return false;
                }
                String fileExtension = fileName.substring(fileName.lastIndexOf(".") + 1);
                if (fileName.length() > 0 && fileName.startsWith(resourceBaseName)
                        && fileExtension.toLowerCase().equals("properties")) {
                    return true;
                }
                return false;
            }
        };
        for (File resourceFile : themeDir.listFiles(fileFilter)) {
            Locale locale = parseResourceLocale(resourceBaseName, resourceFile);
            locales.put(locale.toString(), locale);
        }
        return locales;
    }

    private static Locale parseResourceLocale(String resourceBaseName, File resourceFile) {
        String fileName = resourceFile.getName();
        // default resource (en_US)
        if (fileName.equals(resourceBaseName + ".properties")) {
            return Locale.US;
        } else {
            int lastDotPosition = fileName.lastIndexOf(".");
            int startLocalePostfix = resourceBaseName.length() + 1;
            String localePostfixString = fileName.substring(startLocalePostfix, lastDotPosition);
            String[] localePostfix = localePostfixString.split("_");
            if (localePostfix.length == 1) {
                return new Locale(localePostfix[0]);
            } else if (localePostfix.length == 2) {
                return new Locale(localePostfix[0], localePostfix[1]);
            } else if (localePostfix.length == 3) {
                return new Locale(localePostfix[0], localePostfix[1], localePostfix[2]);
            } else {
                return null;
            }
        }
    }

    public static Map<String, String> setupThemes(String rootPath) {
        Map<String, String> themes = new TreeMap<>();
        File themeDir = new File(rootPath, "resources/theme/");
        FileFilter fileFilter = new FileFilter() {
            @Override
            public boolean accept(File file) {
                String fileName = file.getName();
                if (fileName.length() == 0 || fileName.lastIndexOf(".") == -1) {
                    return false;
                }
                String fileExtension = fileName.substring(fileName.lastIndexOf(".") + 1);
                if (fileName.length() > 0 && fileExtension.toLowerCase().equals("css")) {
                    return true;
                }
                return false;
            }
        };
        try {
            for (File themeFile : themeDir.listFiles(fileFilter)) {
                themes.put(getFileNameExcludeExtension(themeFile), themeFile.toURL()
                        .toExternalForm());
            }
            return themes;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getFileNameExtension(File file) {
        return getFileNameExtension(file.getName());
    }

    public static String getFileNameExtension(String fileName) {
        if (fileName.length() == 0 || fileName.lastIndexOf(".") == -1) {
            return null;
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }

    public static String getFileNameExcludeExtension(File file) {
        String fileName = file.getName();
        if (fileName.length() == 0) {
            return null;
        }
        int lastDotPosition = fileName.lastIndexOf(".");
        if (lastDotPosition == -1) {
            return fileName;
        }
        return fileName.substring(0, fileName.lastIndexOf("."));
    }

    public static String[] getFilterExtensions(String[] splittedFilters) {
        List<String> filterExtensionList = Arrays.stream(splittedFilters)
                .map(filter -> filter.substring(filter.lastIndexOf(".") + 1).toLowerCase())
                .collect(Collectors.toList());
        return filterExtensionList.toArray(new String[filterExtensionList.size()]);
    }

    public static void convertEncoding(String sourceFilePath, String targetFilePath,
            String fromEncoding, String toEncoding) {
        // use commons-exec instead of Runtime.getRuntime().exec(cmd) or
        // ProcessBuilder
        // http://stackoverflow.com/questions/3468987/executing-another-application-from-java
        String cmdLineStr = String.format("\"%s\" -f \"%s\" -t \"%s\" \"%s\" > \"%s\"",
                getIconvExecutablePath(), fromEncoding, toEncoding, sourceFilePath, targetFilePath);
        CommandLine cmdLine = new CommandLine(cmdLineStr);
        DefaultExecutor executor = new DefaultExecutor();
        try {
            System.out.println(cmdLineStr);
            // need to redirect the output to a specify file
            // see
            // https://darthanthony.wordpress.com/2009/06/19/running-a-command-line-executable-in-java-with-redirected-output/
            // int exitValue = executor.execute(cmdLine);
            // executor.execute(cmdLine, new DefaultExecuteResultHandler());
            Runtime.getRuntime().exec(cmdLineStr);
            // Process p = Runtime.getRuntime().exec(cmdLineStr);
            // p.waitFor();
            // int exitVal = p.exitValue();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String iconvExecutablePath = null;

    private static String getIconvExecutablePath() {
        if (iconvExecutablePath == null) {
            Map<String, String> procEnvironment;
            try {
                procEnvironment = EnvironmentUtils.getProcEnvironment();
                String pathEnv = procEnvironment.get("PATH");
                String[] paths = pathEnv.split(File.pathSeparator);
                String iconvName = isWindows() ? "iconv.exe" : "iconv";
                for (String path : paths) {
                    File assumeIconvFile = new File(path, iconvName);
                    if (assumeIconvFile.exists()) {
                        iconvExecutablePath = assumeIconvFile.getAbsolutePath();
                        break;
                    }
                }
                if (isWindows()) {
                    String rootPath = new File(Utils.class.getProtectionDomain().getCodeSource()
                            .getLocation().getPath()).getParent();
                    iconvExecutablePath = new File(rootPath, "resources/bin/iconv.exe")
                            .getAbsolutePath();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return iconvExecutablePath;
    }

    private static boolean isWindows() {
        return System.getProperty("os.name").startsWith("Windows");
    }

    public static String getCustomConvertTargetFilePath(String rootPath, String outputRootPath,
            String filePath) {
        String subPath = filePath.substring(rootPath.length());
        return outputRootPath + subPath;
    }

    public static String getDefaultConvertTargetFilePath(String rootPath, String filePath) {
        String subPath = filePath.substring(rootPath.length());
        String getBakedRootPath = rootPath + "_bak";
        return getBakedRootPath + subPath;
    }

    public static void createDirIfNeeded(String filePath) {
        File directory = new File(filePath).getParentFile();
        if (!directory.exists()) {
            directory.mkdirs();
        }
    }

    public static void copyFile(String sourceFilePath, String targetFilePath) {
        try {
            Files.copy(Paths.get(sourceFilePath), Paths.get(targetFilePath),
                    StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

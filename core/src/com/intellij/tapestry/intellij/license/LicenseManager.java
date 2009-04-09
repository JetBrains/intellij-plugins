package com.intellij.tapestry.intellij.license;
//
//import com.intellij.openapi.application.PathManager;
//import com.intellij.openapi.ui.Messages;
//import com.logical.licensemanager.InvalidLicenseException;
//import com.logical.licensemanager.License;
//import com.logical.licensemanager.LicenseParser;
//import org.apache.log4j.Logger;
//
//import java.awt.*;
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.FileOutputStream;
//import java.io.IOException;
//
public class LicenseManager {
//
//    private static final String LICENSE_FILE_NAME = "tapestry.key";
//
//    private static final Logger _logger = Logger.getLogger(LicenseManager.class);
//
//    public static License getLicense(String pluginVersion) {
//        License license = null;
//        InvalidLicense dialog = null;
//
//        try {
//            license = loadLicense();
//
//            int licenseFlag = license.isValid(pluginVersion);
//
//            switch (licenseFlag) {
//                case License.LICENSE_EXPIRED:
//                    dialog = new InvalidLicense("\nYour Loomy license has expired on " + license.getExpirationDate().toString());
//                    break;
//                case License.LICENSE_NOT_VALID_FOR_VERSION:
//                    dialog = new InvalidLicense("\nYour Loomy license is only valid until version " + license.getVersion());
//                    break;
//            }
//        } catch (Exception ex) {
//            _logger.info("Invalid license", ex);
//
//            dialog = new InvalidLicense(
//                    "\n\"" + PathManager.getConfigPath() + File.separator + LICENSE_FILE_NAME + "\" either doesn't exist or is invalid!"
//            );
//        }
//
//        if (dialog != null) {
//            dialog.pack();
//
//            Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
//
//            // Determine the new location of the window
//            int w = dialog.getSize().width;
//            int h = dialog.getSize().height;
//            int x = (dim.width - w) / 2;
//            int y = (dim.height - h) / 2;
//
//            // Move the window
//            dialog.setLocation(x, y);
//
//            dialog.setModal(true);
//
//            dialog.show();
//
//            if (dialog.isOk()) {
//                try {
//                    LicenseParser.getLicense(dialog.getLicense());
//                } catch (InvalidLicenseException invalidLicenseException) {
//                    return getLicense(pluginVersion);
//                }
//
//                /*LicenseAgreement licenseAgreement = new LicenseAgreement();
//                licenseAgreement.setSize(400, 500);
//
//                w = licenseAgreement.getSize().width;
//                h = licenseAgreement.getSize().height;
//                x = (dim.width - w) / 2;
//                y = (dim.height - h) / 2;
//
//                licenseAgreement.setLocation(x, y);
//                licenseAgreement.setVisible(true);
//
//                if (licenseAgreement.isOk()) {*/
//                try {
//                    saveLicense(dialog.getLicense());
//                } catch (IOException ioException) {
//                    Messages.showErrorDialog(ioException.getMessage(), "Error saving license file");
//                    return getLicense(pluginVersion);
//                }
//                /*} else
//                    return null;*/
//                return getLicense(pluginVersion);
//            } else
//                return null;
//        }
//        return license;
//    }
//
//    private static License loadLicense() throws IOException, InvalidLicenseException {
//        byte[] licenseBytes;
//        FileInputStream fis = new FileInputStream(PathManager.getConfigPath() + File.separator + LICENSE_FILE_NAME);
//
//        licenseBytes = new byte[fis.available()];
//
//        fis.read(licenseBytes, 0, licenseBytes.length);
//        fis.close();
//
//        return LicenseParser.getLicense(new String(licenseBytes));
//    }
//
//    private static void saveLicense(String license) throws IOException {
//        FileOutputStream fos = new FileOutputStream(PathManager.getConfigPath() + File.separator + LICENSE_FILE_NAME);
//        fos.write(license.getBytes());
//
//        fos.close();
//    }
}

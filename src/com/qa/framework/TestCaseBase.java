package com.qa.framework;

import com.qa.framework.cache.DriverCache;
import com.qa.framework.cache.MethodCache;
import com.qa.framework.config.DriverConfig;
import com.qa.framework.config.PropConfig;
import com.qa.framework.data.SuiteData;
import com.qa.framework.ioc.ClassFinder;
import com.qa.framework.library.base.StringHelper;
import com.qa.framework.testnglistener.PowerEmailableReporter;
import com.qa.framework.testnglistener.TestResultListener;
import org.apache.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.annotations.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import static com.qa.framework.ioc.AutoInjectHelper.initFields;
import static com.qa.framework.ioc.IocHelper.findImplementClass;

/**
 * Created by apple on 15/10/16.
 */
@Listeners({TestResultListener.class, PowerEmailableReporter.class})
public abstract class TestCaseBase {
    public WebDriver driver;
    protected Logger logger = Logger.getLogger(this.getClass());
    private SuiteData suiteData = null;

    public WebDriver getDriver() {
        return driver;
    }

    @BeforeSuite(alwaysRun = true)
    public void BeforeSuite() throws Exception {
        logger.info("beforeSuite");
        HelperLoader.init();
        Class<?> clazz = findImplementClass(SuiteData.class);
        if (clazz != null) {
            suiteData = (SuiteData) clazz.newInstance();
            suiteData.setup();
        }
        if (PropConfig.getCoreType().equalsIgnoreCase("ANDROIDAPP") || PropConfig.getCoreType().equalsIgnoreCase("IOSAPP")) {
            driver = DriverConfig.getDriverObject();
            DriverCache.set(driver);
        }
        beforeSuite();
    }

    public void beforeSuite() {
    }

    @AfterSuite(alwaysRun = true)
    public void AfterSuite() throws Exception {
        logger.info("afterSuite");
        if (PropConfig.getCoreType().equalsIgnoreCase("ANDROIDAPP") || PropConfig.getCoreType().equalsIgnoreCase("IOSAPP")) {
            driver.quit();
        }
        Class<?> clazz = findImplementClass(SuiteData.class);
        if (clazz != null) {
            suiteData = (SuiteData) clazz.newInstance();
            suiteData.teardown();
        }
        afterSuite();
    }

    public void afterSuite() {
    }

    @Parameters({"browser", "hubURL"})
    @org.testng.annotations.BeforeClass(alwaysRun = true)
    public void BeforeClass(@Optional String browser, @Optional String hubURL) throws Exception {
        logger.info("beforeClass");
        if (!isUnitTest()) {
            if (!(PropConfig.getCoreType().equalsIgnoreCase("ANDROIDAPP") || PropConfig.getCoreType().equalsIgnoreCase("IOSAPP"))) {
                if (hubURL != null) {
                    DesiredCapabilities capability = null;
                    if (browser.contains("firefox")) {
                        capability = DesiredCapabilities.firefox();
                    } else if (browser.contains("chrome")) {
                        capability = DesiredCapabilities.chrome();
                    }
                    try {
                        driver = new RemoteWebDriver(new URL(hubURL), capability);
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    }
                } else {
                    driver = DriverConfig.getDriverObject();
                }
                DriverCache.set(driver);
            }
        }
        initFields(this);
        beforeClass();
    }

    public void beforeClass() {
    }

    public boolean isUnitTest() {
        return false;
    }

    @AfterClass(alwaysRun = true)
    public void AfterClass() {
        logger.info("afterClass");
        if (!isUnitTest()) {
            if (!(PropConfig.getCoreType().equalsIgnoreCase("ANDROIDAPP") || PropConfig.getCoreType().equalsIgnoreCase("IOSAPP"))) {
                driver.quit();
            }
        }
        afterClass();
    }

    public void afterClass() {
    }

    @BeforeMethod(alwaysRun = true)
    public void BeforeMethod(Method method, Object[] para) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        String currentMethodName;
        if (para != null && para.length > 0 && para[0] != null) {
            currentMethodName = method.getName() + "_" + para[0].toString().trim();
        } else {
            currentMethodName = method.getName();
        }
        MethodCache.set(StringHelper.removeSpecialChar(currentMethodName));
        if (!isUnitTest()) {
            if (!(PropConfig.getCoreType().equalsIgnoreCase("ANDROIDAPP") || PropConfig.getCoreType().equalsIgnoreCase("IOSAPP"))) {
                driver.manage().deleteAllCookies();
            }
        }
        beforeMethod(method, para);
    }

    public void beforeMethod(Method method, Object[] para) {
    }

    @AfterMethod(alwaysRun = true)
    public void AfterMethod(Method method, Object[] para) {
        afterMethod(method, para);
    }

    public void afterMethod(Method method, Object[] para) {
    }

}
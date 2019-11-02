package org.gluu.oxd.server;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.util.concurrent.TimeUnit;

public class SeleniumTester {

    public static void loginGluuServer(WebDriver driver, String opHost, String userId, String userSecret) {
        //navigate to opHost
        driver.navigate().to(opHost);
        driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
        WebElement formElement = driver.findElement(By.name("loginForm"));
        //username field
        WebElement usernameElement = driver.findElement(By.id("loginForm:username"));
        usernameElement.sendKeys(userId);
        //password field
        WebElement passwordElement = driver.findElement(By.id("loginForm:password"));
        passwordElement.sendKeys(userSecret);
        //click on login button
        WebElement loginButton = driver.findElement(By.name("loginForm:loginButton"));

        loginButton.click();
    }

    public static void enableTrustedClient(WebDriver driver, String opHost, String clientId) {
        driver.navigate().to(opHost + "/identity/home.htm");
        driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
        driver.navigate().to(opHost + "/identity/client/updateClient.htm?inum=" + clientId);
        driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
        WebElement trustedClientCheckbox = driver.findElement(By.id("clientForm:oxAuthTrustedClientBox:trustedClientId"));

        if (!trustedClientCheckbox.isSelected()) {
            trustedClientCheckbox.click();

            WebElement buttonElement = driver.findElement(By.className("saveButtonClass"));
            buttonElement.click();
        }
    }

    @Parameters({"opHost", "userId", "userSecret", "clientId", "clientSecret"})
    @Test
    public static void enableTrustedClientOnGluuServer(String opHost, String userId, String userSecret, String clientId, String clientSecret) {
        WebDriver driver = new HtmlUnitDriver();

        loginGluuServer(driver, opHost, userId, userSecret);
        enableTrustedClient(driver, opHost, clientId);

        driver.quit();
    }
}

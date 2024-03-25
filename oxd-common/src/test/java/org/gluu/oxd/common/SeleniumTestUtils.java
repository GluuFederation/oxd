package org.gluu.oxd.common;

import org.apache.commons.collections.CollectionUtils;
import org.gluu.oxauth.client.AuthorizationRequest;
import org.gluu.oxauth.client.AuthorizationResponse;
import org.gluu.oxauth.model.common.AuthorizationMethod;
import org.gluu.oxauth.model.common.Holder;
import org.gluu.oxauth.model.common.Prompt;
import org.gluu.oxauth.model.common.ResponseType;
import org.gluu.oxauth.model.util.Util;
import org.gluu.oxd.common.model.AuthenticationDetails;
import org.openqa.selenium.*;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.collections.Lists;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.testng.Assert.fail;

public class SeleniumTestUtils {

    private static int WAIT_OPERATION_TIMEOUT = 30;
    private static final Logger LOG = LoggerFactory.getLogger(SeleniumTestUtils.class);

    public static AuthorizationResponse authorizeClient(AuthenticationDetails authenticationDetails, List<String> responseTypes, List<String> scopes) {
        WebDriver driver = initWebDriver(true, true);

        AuthorizationResponse authorizationResponse = loginGluuServer(driver, authenticationDetails, responseTypes, scopes);
        //AuthorizationResponse authorizationResponse = acceptAuthorization(driver);

        driver.quit();
        return authorizationResponse;
    }

    private static AuthorizationResponse loginGluuServer(
            WebDriver driver, AuthenticationDetails authenticationDetails, List<String> responseTypes, List<String> scopes) {
        //navigate to opHost

        String authzUrl = getAuthorizationUrl(authenticationDetails, responseTypes, scopes);
        System.out.println(authzUrl);
        driver.navigate().to(authzUrl);

        //driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
        Wait<WebDriver> wait = new FluentWait<WebDriver>(driver)
                .withTimeout(Duration.ofSeconds(WAIT_OPERATION_TIMEOUT))
                .pollingEvery(Duration.ofMillis(500))
                .ignoring(NoSuchElementException.class);

        WebElement allowButton = wait.until(new Function<WebDriver, WebElement>() {
            public WebElement apply(WebDriver d) {
                //System.out.println(d.getCurrentUrl());
                //System.out.println(d.getPageSource());
                return d.findElement(By.id("authorizeForm:allowButton"));
            }
        });
        String authorizationResponseStr = driver.getCurrentUrl();
        // We have to use JavaScript because target is link with onclick
        JavascriptExecutor jse = (JavascriptExecutor) driver;
        jse.executeScript("scroll(0, 1000)");

        String previousURL = driver.getCurrentUrl();

        Actions actions = new Actions(driver);
        actions.click(allowButton).perform();

        authorizationResponseStr = driver.getCurrentUrl();
        AuthorizationResponse authorizationResponse = new AuthorizationResponse(authorizationResponseStr);

        LOG.info("Authorization Response url is: " + driver.getCurrentUrl());

        return authorizationResponse;

    }

    /*private static AuthorizationResponse acceptAuthorization(WebDriver driver) {
        String authorizationResponseStr = driver.getCurrentUrl();
        AuthorizationResponse authorizationResponse = null;
        // Check for authorization form if client has no persistent authorization
        if (!authorizationResponseStr.contains("#")) {
            Wait<WebDriver> wait = new FluentWait<WebDriver>(driver)
                    .withTimeout(Duration.ofSeconds(WAIT_OPERATION_TIMEOUT))
                    .pollingEvery(Duration.ofMillis(500))
                    .ignoring(NoSuchElementException.class);

            WebElement allowButton = wait.until(new Function<WebDriver, WebElement>() {
                public WebElement apply(WebDriver d) {
                    //System.out.println(d.getCurrentUrl());
                    //System.out.println(d.getPageSource());
                    return d.findElement(By.id("authorizeForm:allowButton"));
                }
            });

            // We have to use JavaScript because target is link with onclick
            JavascriptExecutor jse = (JavascriptExecutor) driver;
            jse.executeScript("scroll(0, 1000)");

            String previousURL = driver.getCurrentUrl();

            Actions actions = new Actions(driver);
            actions.click(allowButton).perform();

            authorizationResponseStr = driver.getCurrentUrl();
            authorizationResponse = new AuthorizationResponse(authorizationResponseStr);

            LOG.info("Authorization Response url is: " + driver.getCurrentUrl());
        } else {
            fail("The authorization form was expected to be shown.");
        }
        return authorizationResponse;
    }*/

    private static WebDriver initWebDriver(boolean enableJavascript, boolean cleanupCookies) {
        WebDriver currentDriver = new HtmlUnitDriver();
        ((HtmlUnitDriver) currentDriver).setJavascriptEnabled(enableJavascript);
        try {
            if (cleanupCookies) {
                currentDriver.manage().deleteAllCookies();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return currentDriver;
    }

    private static String getAuthorizationUrl(AuthenticationDetails authenticationDetails, List<String> responseTypes, List<String> scopes) {
        try {
            if (CollectionUtils.isEmpty(responseTypes)) {
                responseTypes = Lists.newArrayList("code", "id_token", "token");
            }

            if (CollectionUtils.isEmpty(scopes)) {
                scopes = Lists.newArrayList("openid", "profile", "oxd", "uma_protection");
            }
            List<ResponseType> resTypes = responseTypes.stream().map(item -> ResponseType.fromString(item)).collect(Collectors.toList());
            AuthorizationRequest authorizationRequest = new AuthorizationRequest(resTypes, authenticationDetails.getClientId(), scopes, authenticationDetails.getRedirectUrls().split(" ")[0], authenticationDetails.getNonce());
            authorizationRequest.setResponseTypes(responseTypes.stream().map(item -> ResponseType.fromString(item)).collect(Collectors.toList()));
            authorizationRequest.setState(authenticationDetails.getState());
            authorizationRequest.addCustomParameter("mail", authenticationDetails.getUserEmail());
            authorizationRequest.addCustomParameter("inum", authenticationDetails.getUserInum());
            authorizationRequest.getPrompts().add(Prompt.NONE);
            authorizationRequest.setAuthorizationMethod(AuthorizationMethod.FORM_ENCODED_BODY_PARAMETER);

            return URLDecoder.decode(authenticationDetails.getOpHost() + "/oxauth/restv1/authorize?" + authorizationRequest.getQueryString(), Util.UTF8_STRING_ENCODING);

        } catch (UnsupportedEncodingException ex) {
            fail("Failed to decode the authorization URL.");
            return null;
        }
    }

    private static String waitForPageSwitch(WebDriver currentDriver, String previousURL) {
        Holder<String> currentUrl = new Holder<>();
        WebDriverWait wait = new WebDriverWait(currentDriver, WAIT_OPERATION_TIMEOUT);
        wait.until(d -> {
            //System.out.println("Previous url: " + previousURL);
            //System.out.println("Current url: " + d.getCurrentUrl());
            currentUrl.setT(d.getCurrentUrl());
            return !currentUrl.getT().equals(previousURL);
        });
        return currentUrl.getT();
    }
}

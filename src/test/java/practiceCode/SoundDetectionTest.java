package practiceCode;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.time.Duration;

public class SoundDetectionTest {
    private WebDriver driver;
    private WebDriverWait wait;

    // Element locators
    private static final By CHARGE_INPUT = By.id("charge-percentage");
    private static final By UPDATE_BUTTON = By.xpath("//button[text()='Update']");
    private static final By BEEP_NOTIFICATION = By.id("beep-notification");
    private static final By ACKNOWLEDGE_BUTTON = By.xpath("//button[text()='Acknowledge']");

    @BeforeClass
    public void setUp() {
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
        driver.manage().window().maximize();
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @Test(priority = 1)
    public void testBatteryColor() throws InterruptedException {
        String filePath = new File("src/test/resources/BatteryNotification.html").getAbsolutePath();
        String fileUrl = "file:///" + filePath.replace("\\", "/");
        driver.get(fileUrl);
//        driver.get("file:///C:/Users/SIVA/Desktop/BatteryNotification.html");

        setBatteryPercentage(25);
        driver.findElement(UPDATE_BUTTON).click();

        String backgroundColor = getBatteryColor();
        int batteryWidth = getBatteryWidth();

        if (batteryWidth < 20) {
            handleLowBatteryScenario(backgroundColor);
        } else {
            handleSufficientBatteryScenario();
        }
    }

    private void setBatteryPercentage(int percentage) {
        WebElement chargeInput = driver.findElement(CHARGE_INPUT);
        chargeInput.clear();
        chargeInput.sendKeys(String.valueOf(percentage));
    }

    private String getBatteryColor() {
        return (String) ((JavascriptExecutor) driver)
                .executeScript("return document.getElementById('battery').style.backgroundColor");
    }

    private int getBatteryWidth() {
        String width = (String) ((JavascriptExecutor) driver)
                .executeScript("return document.getElementById('battery').style.width");
        return Integer.parseInt(width.replace("%", ""));
    }

    private void handleLowBatteryScenario(String backgroundColor) throws InterruptedException {
        wait.until(ExpectedConditions.visibilityOfElementLocated(ACKNOWLEDGE_BUTTON));
        Assert.assertEquals(backgroundColor, "red", "Battery color should be red when below 20%");

        wait.until(ExpectedConditions.visibilityOfElementLocated(BEEP_NOTIFICATION));
        Assert.assertTrue(driver.findElement(BEEP_NOTIFICATION).isDisplayed(),
                "Beep notification should be displayed for low battery percentage.");

        verifyAudioPlaying();
        Thread.sleep(6000);
        // Simulate user acknowledging the notification
        driver.findElement(ACKNOWLEDGE_BUTTON).click();
        Thread.sleep(2000);  // Simulate delay for user action

        System.out.println("Your battery is in: " + backgroundColor + " color.");
        System.out.println("Your battery is at: " + getBatteryWidth() + "% charging. Please charge it immediately.");
    }

    private void handleSufficientBatteryScenario() {
        String backgroundColor = getBatteryColor();
        System.out.println("Your battery is in: " + backgroundColor + " color.");
        System.out.println("Your battery is at: " + getBatteryWidth() + "% charging. Always keep your battery above 20% charge.");
    }

    private void verifyAudioPlaying() {
        boolean isAudioPlaying = (Boolean) ((JavascriptExecutor) driver)
                .executeScript("var audio = document.getElementById('beep-sound'); return !audio.paused;");

        if(isAudioPlaying){
            System.out.println("Beep warning informing you to charge your battery immediately.");
            Assert.assertTrue(isAudioPlaying, "Audio should be playing when battery percentage is below 20%.");
        }else {
            System.out.println("Audio is not playing.");
            Assert.assertTrue(false, "Audio should be playing when battery percentage is below 20%.");
        }


    }

    @AfterClass
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}
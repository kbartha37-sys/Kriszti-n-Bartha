package com.example

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class GreetingScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun greeting_screenshot() {
    composeTestRule.setContent {
      MaterialTheme(
        colorScheme = darkColorScheme(
          primary = PitchGreenAccent,
          secondary = StadiumGoldAccent,
          background = EmeraldNightBackground,
          surface = EmeraldSurfaceCard,
          onBackground = ElectricWhite,
          onSurface = ElectricWhite
        )
      ) {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .background(EmeraldNightBackground)
            .padding(24.dp),
          contentAlignment = Alignment.Center
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
              text = "MATCH PREDICTOR AI",
              fontSize = 24.sp,
              fontWeight = FontWeight.Bold,
              color = ElectricWhite
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = "Ready to predict matches!",
              fontSize = 16.sp,
              color = SlateGrayText
            )
          }
        }
      }
    }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/greeting.png")
  }
}

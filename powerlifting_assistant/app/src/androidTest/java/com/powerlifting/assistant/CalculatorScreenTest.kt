package com.powerlifting.assistant

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.material3.MaterialTheme
import com.powerlifting.assistant.presentation.screens.CalculatorScreen
import org.junit.Rule
import org.junit.Test

class CalculatorScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun calculatorScreenShowsTitle() {
        composeTestRule.setContent {
            MaterialTheme {
                CalculatorScreen()
            }
        }

        composeTestRule.onNodeWithText("Калькулятор веса штанги").assertIsDisplayed()
    }

    @Test
    fun calculatorShowsBarWeightChips() {
        composeTestRule.setContent {
            MaterialTheme {
                CalculatorScreen()
            }
        }

        composeTestRule.onNodeWithText("20 кг").assertIsDisplayed()
        composeTestRule.onNodeWithText("15 кг").assertIsDisplayed()
        composeTestRule.onNodeWithText("10 кг").assertIsDisplayed()
    }

    @Test
    fun calculatorShowsErrorForLargeWeight() {
        composeTestRule.setContent {
            MaterialTheme {
                CalculatorScreen()
            }
        }

        composeTestRule.onNodeWithText("Желаемый вес штанги (кг)").performClick()
        composeTestRule.onNode(hasSetTextAction()).performTextInput("99999")

        composeTestRule.onNodeWithText("Максимальный вес — 600 кг.", ignoreCase = true).assertIsDisplayed()
    }

    @Test
    fun calculatorShowsCalculateButton() {
        composeTestRule.setContent {
            MaterialTheme {
                CalculatorScreen()
            }
        }

        composeTestRule.onNodeWithText("Рассчитать").assertIsDisplayed()
    }
}

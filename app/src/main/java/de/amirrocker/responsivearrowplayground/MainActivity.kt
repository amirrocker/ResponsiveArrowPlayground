package de.amirrocker.responsivearrowplayground

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import de.amirrocker.responsivearrowplayground.scratchpad.testBasicOptics
import de.amirrocker.responsivearrowplayground.scratchpad.testSimpleLambda
import de.amirrocker.responsivearrowplayground.scratchpad.testComposeMultiply
import de.amirrocker.responsivearrowplayground.scratchpad.testComposition
import de.amirrocker.responsivearrowplayground.scratchpad.testLensAccessors
import de.amirrocker.responsivearrowplayground.scratchpad.testSimpleExtensions
import de.amirrocker.responsivearrowplayground.ui.theme.ResponsiveArrowPlaygroundTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        testSimpleLambda()
//        testComposeMultiply()
        testBasicOptics()
        testLensAccessors()
        testComposition()
        testSimpleExtensions()

        /*
        enableEdgeToEdge()
        setContent {
            ResponsiveArrowPlaygroundTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AnimatedBoxOffset(modifier = Modifier.padding(innerPadding))
//                    AnimatedBoxOffset()
//                    EventSourcing()
                }
            }
        }
         */
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {

    Column(
        modifier = modifier
            .fillMaxSize()
    ) {

        val colorStops = arrayOf(
            0.0f to Color.White,
            0.05f to Color.Transparent,
            0.95f to Color.Transparent,
            1.0f to Color.White,
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.1f)
                .drawWithContent {
                    drawContent()
                    drawRect(
                        brush = Brush.horizontalGradient(
                            colorStops = colorStops,
                        )
                    )
                },
            contentAlignment = Alignment.CenterStart,
        ) {
            Text(
                text = "Hello $name!",
                modifier = modifier
            )
        }
        Text(
            text = "Hello $name!",
            modifier = modifier
        )
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    ResponsiveArrowPlaygroundTheme {
        Greeting("Android")
    }
}
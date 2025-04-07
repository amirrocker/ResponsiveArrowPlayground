package de.amirrocker.responsivearrowplayground.scratchpad

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

//@Composable
//fun AnimatedBoxOffset() {
//
//    var clicked by remember { mutableStateOf(false) }
//
//    val offset by animateIntOffsetAsState(
//        targetValue = if(clicked) {
//            IntOffset(300, 300)
//        } else {
//            IntOffset(150, 150)
//        },
//        finishedListener = {
//            println("DONE DONE")
//        }
//    )
//
//    Box(
//        modifier = Modifier
//            .size(100.dp)
//            .background(Color.Blue)
//            .clickable {
//                clicked = true
//            }
//            .offset {
//                offset
//            }
//    )
//}

@Composable
fun AnimatedBoxOffset(
    modifier: Modifier = Modifier
) {
    var visible by remember { mutableStateOf(true) }
    var stateVisible by remember { mutableStateOf(false) }
    var buttonVisible by remember { mutableStateOf(true) }
    val density = LocalDensity.current

    // use MutableTransitionState<T> for AnimatedVisibilty
    var state = remember {
        MutableTransitionState<Boolean>(stateVisible).apply {
            // start animation at once
            targetState = stateVisible
        }
    }

    Column {
        Spacer(modifier = modifier.height(40.dp))
        AnimatedVisibility(
            visible = visible,
            enter = slideInHorizontally {
                with(density) {
                    -40.dp.roundToPx()
                }
            } + expandHorizontally(
                expandFrom = Alignment.Start
            ) + fadeIn(
                initialAlpha = 0.3f
            ),
            exit = slideOutHorizontally() + shrinkHorizontally() + fadeOut()
        ) {
            Box(
                modifier = modifier
                    .size(100.dp)
                    .background(Color.Blue)
                    .clickable {
                        buttonVisible = !buttonVisible
                    }
            )
        }
        AnimatedVisibility(
            visible = buttonVisible,
        ) {
            Button(
                onClick = {
                    visible = !visible
                }
            ) {
                Text("make visible")
            }
        }

        // MutableTransitionState
        Button(
            onClick = {
                state.targetState = !stateVisible
            }
        ) {
            Text("start Animation")
        }
        AnimatedVisibility(visibleState = state) {
            Text("Hello MutableTransitionState")
        }
        Text(
            text = when {
                state.isIdle && state.currentState -> "Visible state"
                !state.isIdle && state.currentState -> "Disappearing state"
                state.isIdle && !state.currentState -> "Invisible state"
                else -> "Appearing"
            }
        )
        AnimateEnterExitWithChildren()
    }
}

@Composable
fun AnimateEnterExitWithChildren() {
    var visible by remember { mutableStateOf(true) }
    Column {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(
                animationSpec = TweenSpec(
                    1000
                )
            ),
            exit = fadeOut()
        ) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .background(Color.DarkGray)
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .animateEnterExit(
                            enter = slideInVertically(),
                            exit = slideOutVertically()
                        )
                        .sizeIn(minWidth = 256.dp, minHeight = 64.dp)
                        .background(Color.Red)
                ) {

                    Text("Inner Box")


                }
            }
        }
        Button(
            onClick = {
                visible = !visible
            }
        ) {
            Text("start Animation")
        }
    }
}

// use this to correct the ugly upper button behaviors
// once the content faded out, the buttons jump.
// go on here
// https://developer.android.com/develop/ui/compose/animation/composables-modifiers#animatedvisibility
@Composable
fun AnimateWithAnimatedContent() {

}




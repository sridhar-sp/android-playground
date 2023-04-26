package com.gandiva.aidl.client.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.UiComposable
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

@Composable
fun FlexRow(
    modifier: Modifier = Modifier,
    padding: PaddingValues = PaddingValues(all = 0.dp),
    content: @Composable @UiComposable () -> Unit
) {


    Layout(
        modifier = modifier,
        content = content
    ) { measurables, constraints ->

        fun verticalPadding() = (padding.calculateTopPadding() + padding.calculateBottomPadding()).toPx().toInt()

        // Don't constrain child views further, measure them with given constraints
        // List of measured children
        var requiredHeight = 0
        var measureX = 0
        val placeables = measurables.map { measurable ->
            val placeable = measurable.measure(constraints)
            measureX += placeable.width + padding.calculateRightPadding(LayoutDirection.Ltr).toPx().toInt()

            if (measureX > (constraints.maxWidth - placeable.width)) {
                measureX = 0
            }

            if (measureX == 0 || requiredHeight == 0) {
                requiredHeight += placeable.height + verticalPadding()
            }
            placeable

        }

        // Set the size of the layout as big as it can
        layout(constraints.maxWidth, requiredHeight) {
            // Track the y co-ord we have placed children up to
            var yPosition = 0
            var xPosition = 0


            // Place children in the parent layout
            placeables.forEach { placeable ->
                // Move to next line to place children when running out of width
                if (placeable.width > (constraints.maxWidth - xPosition)) {
                    xPosition = 0
                    yPosition += placeable.height + verticalPadding()
                }

                xPosition += padding.calculateLeftPadding(LayoutDirection.Ltr).toPx().toInt()
                // Position item on the screen
                placeable.placeRelative(x = xPosition, y = yPosition)

                // Record the y co-ord placed up to
                xPosition += placeable.width + padding.calculateRightPadding(LayoutDirection.Ltr).toPx().toInt()
            }
        }
    }
}
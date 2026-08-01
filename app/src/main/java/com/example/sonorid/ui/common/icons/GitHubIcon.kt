// app/src/main/java/com/example/sonorid/ui/common/icons/GitHubIcon.kt
package com.example.sonorid.ui.common.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/** Logo oficial de GitHub (el "Octocat" simplificado), como ImageVector propio
 * ya que no forma parte de Material Icons. */
val GitHubIcon: ImageVector by lazy {
    ImageVector.Builder(
        name = "GitHub",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(
            fill = androidx.compose.ui.graphics.SolidColor(Color.Black),
            pathFillType = PathFillType.NonZero
        ) {
            moveTo(12f, 0f)
            curveTo(5.37f, 0f, 0f, 5.37f, 0f, 12f)
            curveTo(0f, 17.31f, 3.435f, 21.795f, 8.205f, 23.385f)
            curveTo(8.805f, 23.49f, 9.03f, 23.13f, 9.03f, 22.815f)
            curveTo(9.03f, 22.53f, 9.015f, 21.585f, 9.015f, 20.58f)
            curveTo(6f, 21.135f, 5.22f, 19.845f, 4.98f, 19.17f)
            curveTo(4.845f, 18.825f, 4.26f, 17.76f, 3.75f, 17.475f)
            curveTo(3.33f, 17.25f, 2.73f, 16.695f, 3.735f, 16.68f)
            curveTo(4.68f, 16.665f, 5.355f, 17.55f, 5.58f, 17.91f)
            curveTo(6.66f, 19.725f, 8.385f, 19.215f, 9.075f, 18.9f)
            curveTo(9.18f, 18.12f, 9.495f, 17.595f, 9.84f, 17.295f)
            curveTo(7.17f, 16.995f, 4.38f, 15.96f, 4.38f, 11.37f)
            curveTo(4.38f, 10.065f, 4.845f, 8.985f, 5.61f, 8.145f)
            curveTo(5.49f, 7.845f, 5.085f, 6.645f, 5.625f, 5.01f)
            curveTo(5.625f, 5.01f, 6.63f, 4.695f, 8.925f, 6.24f)
            curveTo(9.885f, 5.97f, 10.905f, 5.835f, 11.925f, 5.835f)
            curveTo(12.945f, 5.835f, 13.965f, 5.97f, 14.925f, 6.24f)
            curveTo(17.22f, 4.68f, 18.225f, 5.01f, 18.225f, 5.01f)
            curveTo(18.765f, 6.645f, 18.36f, 7.845f, 18.24f, 8.145f)
            curveTo(19.005f, 8.985f, 19.47f, 10.05f, 19.47f, 11.37f)
            curveTo(19.47f, 15.975f, 16.665f, 16.995f, 13.995f, 17.295f)
            curveTo(14.43f, 17.67f, 14.805f, 18.39f, 14.805f, 19.515f)
            curveTo(14.805f, 21.12f, 14.79f, 22.41f, 14.79f, 22.815f)
            curveTo(14.79f, 23.13f, 15.015f, 23.505f, 15.615f, 23.385f)
            curveTo(20.385f, 21.795f, 23.82f, 17.31f, 23.82f, 12f)
            curveTo(24f, 5.37f, 18.63f, 0f, 12f, 0f)
            close()
        }
    }.build()
}
package com.masrofy.app

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp

/** طقم أيقونات مصروفي: أيقونات خطية رفيعة موحّدة (24×24). */
object Ic {
    private fun make(name: String, vararg paths: String): ImageVector {
        val b = ImageVector.Builder(name, 24.dp, 24.dp, 24f, 24f)
        for (p in paths) {
            b.addPath(
                pathData = PathParser().parsePathString(p).toNodes(),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.6f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            )
        }
        return b.build()
    }

    val Up: ImageVector by lazy {
        make("Up",
            "M7 17L17 7",
            "M8 7h9v9"
        )
    }

    val Down: ImageVector by lazy {
        make("Down",
            "M17 7L7 17",
            "M16 17H7V8"
        )
    }

    val Receipt: ImageVector by lazy {
        make("Receipt",
            "M6 4l1.5 1L9 4l1.5 1L12 4l1.5 1L15 4l1.5 1L18 4v16H6z",
            "M9 9h6M9 12h6M9 15h6"
        )
    }

    val Calc: ImageVector by lazy {
        make("Calc",
            "M8.5 3H15.5a2.5 2.5 0 0 1 2.5 2.5V18.5a2.5 2.5 0 0 1 -2.5 2.5H8.5a2.5 2.5 0 0 1 -2.5 -2.5V5.5a2.5 2.5 0 0 1 2.5 -2.5z",
            "M9 7h6M9 12h.01M12 12h.01M15 12h.01M9 16h.01M12 16h.01M15 16h.01"
        )
    }

    val Bell: ImageVector by lazy {
        make("Bell",
            "M6 16v-5a6 6 0 0 1 12 0v5l1.5 2h-15z",
            "M10 20a2 2 0 0 0 4 0"
        )
    }

    val Food: ImageVector by lazy {
        make("Food",
            "M7 3v7a2 2 0 0 0 2 2v9M11 3v7M7 7h4M17 3c-2 2-2 6 0 8v10"
        )
    }

    val Bus: ImageVector by lazy {
        make("Bus",
            "M5 4h14a1 1 0 0 1 1 1v12a1 1 0 0 1-1 1H5a1 1 0 0 1-1-1V5a1 1 0 0 1 1-1z",
            "M4 11h16",
            "M7 18v2.5M17 18v2.5",
            "M8 7.5h8",
            "M7.3 14.5a0.7 0.7 0 1 0 1.4 0a0.7 0.7 0 1 0 -1.4 0z",
            "M15.3 14.5a0.7 0.7 0 1 0 1.4 0a0.7 0.7 0 1 0 -1.4 0z"
        )
    }

    val Bag: ImageVector by lazy {
        make("Bag",
            "M3 10h18l-2 10H5z",
            "M8 10l3-6M16 10l-3-6"
        )
    }

    val Heart: ImageVector by lazy {
        make("Heart",
            "M12 20s-7-4.5-7-10a4 4 0 0 1 7-2.5A4 4 0 0 1 19 10c0 5.5-7 10-7 10z"
        )
    }

    val Home: ImageVector by lazy {
        make("Home",
            "M4 11l8-7 8 7",
            "M6 10v10h12V10"
        )
    }

    val Pie: ImageVector by lazy {
        make("Pie",
            "M3 12a9 9 0 1 0 18 0a9 9 0 1 0 -18 0z",
            "M12 3v9h9"
        )
    }

    val More: ImageVector by lazy {
        make("More",
            "M6 12h.01M12 12h.01M18 12h.01"
        )
    }

    val Plus: ImageVector by lazy {
        make("Plus",
            "M12 5v14M5 12h14"
        )
    }

    val Wallet: ImageVector by lazy {
        make("Wallet",
            "M4 7h14a2 2 0 0 1 2 2v9a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2z",
            "M4 7l12-3M16 13.5h4"
        )
    }

    val Flag: ImageVector by lazy {
        make("Flag",
            "M6 21V4M6 5h11l-2 4 2 4H6"
        )
    }

    val Search: ImageVector by lazy {
        make("Search",
            "M5 11a6 6 0 1 0 12 0a6 6 0 1 0 -12 0z",
            "M20 20l-4-4"
        )
    }

    val Tv: ImageVector by lazy {
        make("Tv",
            "M5.5 5H18.5a2.5 2.5 0 0 1 2.5 2.5V14.5a2.5 2.5 0 0 1 -2.5 2.5H5.5a2.5 2.5 0 0 1 -2.5 -2.5V7.5a2.5 2.5 0 0 1 2.5 -2.5z",
            "M9 21h6"
        )
    }

    val Cash: ImageVector by lazy {
        make("Cash",
            "M6 6H18a3 3 0 0 1 3 3V15a3 3 0 0 1 -3 3H6a3 3 0 0 1 -3 -3V9a3 3 0 0 1 3 -3z",
            "M9.5 12a2.5 2.5 0 1 0 5 0a2.5 2.5 0 1 0 -5 0z"
        )
    }

    val Cafe: ImageVector by lazy {
        make("Cafe",
            "M5 9h11v5a4 4 0 0 1-4 4H9a4 4 0 0 1-4-4z",
            "M16 10.5h1.5a2 2 0 0 1 0 4H16",
            "M4 21h14",
            "M8 3.5c-.8 1 .8 1.5 0 2.5M12 3.5c-.8 1 .8 1.5 0 2.5"
        )
    }

    val ChevRight: ImageVector by lazy {
        make("ChevRight",
            "M9 6l6 6-6 6"
        )
    }

    val Trend: ImageVector by lazy {
        make("Trend",
            "M3 17l6-6 4 4 8-8",
            "M15 7h6v6"
        )
    }

    val ChevLeft: ImageVector by lazy {
        make("ChevLeft",
            "M15 6l-6 6 6 6"
        )
    }

    val Backspace: ImageVector by lazy {
        make("Backspace",
            "M21 6H9l-6 6 6 6h12z",
            "M12 9l5 6M17 9l-5 6"
        )
    }

    val Book: ImageVector by lazy {
        make("Book",
            "M2 9l10-5 10 5-10 5z",
            "M6 11.5V16c0 1.5 3 3 6 3s6-1.5 6-3v-4.5",
            "M22 9v6"
        )
    }

    val Gift: ImageVector by lazy {
        make("Gift",
            "M6 9H18a2 2 0 0 1 2 2V18a2 2 0 0 1 -2 2H6a2 2 0 0 1 -2 -2V11a2 2 0 0 1 2 -2z",
            "M3 9h18v3H3zM12 9v11M12 9c-3 0-4-4-1.5-4S12 9 12 9zM12 9c3 0 4-4 1.5-4S12 9 12 9z"
        )
    }

    val Phone: ImageVector by lazy {
        make("Phone",
            "M5 4h4l2 5-2.5 1.5a11 11 0 0 0 5 5L15 13l5 2v4a2 2 0 0 1-2 2A16 16 0 0 1 3 6a2 2 0 0 1 2-2z"
        )
    }

    val Plane: ImageVector by lazy {
        make("Plane",
            "M21 16v-2l-8-5V3.5a1.5 1.5 0 0 0-3 0V9l-8 5v2l8-2.5V19l-2 1.5V22l3.5-1 3.5 1v-1.5L13 19v-5.5z"
        )
    }

    val Dots: ImageVector by lazy {
        make("Dots",
            "M4.8 12a1.2 1.2 0 1 0 2.4 0a1.2 1.2 0 1 0 -2.4 0z",
            "M10.8 12a1.2 1.2 0 1 0 2.4 0a1.2 1.2 0 1 0 -2.4 0z",
            "M16.8 12a1.2 1.2 0 1 0 2.4 0a1.2 1.2 0 1 0 -2.4 0z"
        )
    }

    val Cam: ImageVector by lazy {
        make("Cam",
            "M4 8h3l2-3h6l2 3h3v11H4z",
            "M8.5 13a3.5 3.5 0 1 0 7 0a3.5 3.5 0 1 0 -7 0z"
        )
    }

    val Target: ImageVector by lazy {
        make("Target",
            "M3 12a9 9 0 1 0 18 0a9 9 0 1 0 -18 0z",
            "M7 12a5 5 0 1 0 10 0a5 5 0 1 0 -10 0z",
            "M12 12l7-7",
            "M16 4v3.5h3.5"
        )
    }

    val Cloud: ImageVector by lazy {
        make("Cloud",
            "M7 18a4 4 0 0 1-.5-8A6 6 0 0 1 18 11a3.5 3.5 0 0 1-.5 7z"
        )
    }

    val Users: ImageVector by lazy {
        make("Users",
            "M5.5 8a3.5 3.5 0 1 0 7 0a3.5 3.5 0 1 0 -7 0z",
            "M2.5 20c.8-3.5 3.3-5 6.5-5s5.7 1.5 6.5 5M16 5a3.5 3.5 0 0 1 0 6.5M18 15c2 .5 3.3 2 3.8 5"
        )
    }

    val Repeat: ImageVector by lazy {
        make("Repeat",
            "M20 12a8 8 0 0 1-13.5 5.8",
            "M4 12a8 8 0 0 1 13.5-5.8",
            "M17.5 3v3.5H14",
            "M6.5 21v-3.5H10"
        )
    }

    val Close: ImageVector by lazy {
        make("Close",
            "M6 6l12 12M18 6L6 18"
        )
    }

    val Check: ImageVector by lazy {
        make("Check",
            "M5 12l5 5 9-10"
        )
    }

    val Edit: ImageVector by lazy {
        make("Edit",
            "M4 20h4L19 9l-4-4L4 16z",
            "M13.5 6.5l4 4"
        )
    }

    val Trash: ImageVector by lazy {
        make("Trash",
            "M5 7h14M10 7V4h4v3M7 7l1 13h8l1-13M10 11v6M14 11v6"
        )
    }

    val Share: ImageVector by lazy {
        make("Share",
            "M12 4v11M8 8l4-4 4 4M5 13v6h14v-6"
        )
    }

    val Upload: ImageVector by lazy {
        make("Upload",
            "M12 16V4M8 8l4-4 4 4M5 20h14"
        )
    }

    val Download: ImageVector by lazy {
        make("Download",
            "M12 4v12M8 12l4 4 4-4M5 20h14"
        )
    }

    val Photo: ImageVector by lazy {
        make("Photo",
            "M5 4H19a2 2 0 0 1 2 2V18a2 2 0 0 1 -2 2H5a2 2 0 0 1 -2 -2V6a2 2 0 0 1 2 -2z",
            "M3 16l5-5 5 5 3-3 5 5",
            "M7.5 9a1.5 1.5 0 1 0 3 0a1.5 1.5 0 1 0 -3 0z"
        )
    }

    val Warning: ImageVector by lazy {
        make("Warning",
            "M12 4l9 16H3z",
            "M12 10v4M12 17h.01"
        )
    }

    val Fwd: ImageVector by lazy {
        make("Fwd",
            "M5 12h14M13 6l6 6-6 6"
        )
    }

    val Attach: ImageVector by lazy {
        make("Attach",
            "M20 11l-8 8a5 5 0 0 1-7-7l8-8a3.5 3.5 0 0 1 5 5l-8 8a2 2 0 0 1-3-3l7-7"
        )
    }

    val Lock: ImageVector by lazy {
        make("Lock",
            "M5 11h14v10H5z",
            "M8 11V7a4 4 0 0 1 7.5-1.5"
        )
    }

    val HeartPulse: ImageVector by lazy {
        make("HeartPulse",
            "M12 20s-7-4.5-7-10a4 4 0 0 1 7-2.5A4 4 0 0 1 19 10c0 5.5-7 10-7 10z",
            "M7 11h3l1.5-3 2 5 1.5-2H17"
        )
    }

    val Cart: ImageVector by lazy {
        make("Cart",
            "M3 4h2.5l2.2 11h10.3l2-8H6.5",
            "M7.8 19.5a1.2 1.2 0 1 0 2.4 0a1.2 1.2 0 1 0 -2.4 0z",
            "M15.8 19.5a1.2 1.2 0 1 0 2.4 0a1.2 1.2 0 1 0 -2.4 0z"
        )
    }

    val Car: ImageVector by lazy {
        make("Car",
            "M5 12l1.6-4.6A2 2 0 0 1 8.5 6h7a2 2 0 0 1 1.9 1.4L19 12",
            "M4 12h16a1 1 0 0 1 1 1v4a1 1 0 0 1-1 1h-1.5v1.5M5.5 18H4a1 1 0 0 1-1-1v-4a1 1 0 0 1 1-1",
            "M5.5 18v1.5",
            "M7 18h10",
            "M6.9 14.8a0.6 0.6 0 1 0 1.2 0a0.6 0.6 0 1 0 -1.2 0z",
            "M15.9 14.8a0.6 0.6 0 1 0 1.2 0a0.6 0.6 0 1 0 -1.2 0z"
        )
    }

    val Gamepad: ImageVector by lazy {
        make("Gamepad",
            "M7 8h10a4 4 0 0 1 3.9 4.9l-.7 3.4A2.2 2.2 0 0 1 16 17l-1-1.5H9L8 17a2.2 2.2 0 0 1-4.200-.7l-.7-3.400A4 4 0 0 1 7 8z",
            "M8 10.500v3M6.500 12h3",
            "M15.500 11.200h.01M17.500 13h.01"
        )
    }

    val Mobile: ImageVector by lazy {
        make("Mobile",
            "M7 2.5h10a1.500 1.500 0 0 1 1.500 1.500v16a1.500 1.500 0 0 1-1.500 1.500H7A1.500 1.500 0 0 1 5.500 20V4A1.500 1.500 0 0 1 7 2.500z",
            "M10.500 18.500h3"
        )
    }

    val BarChart: ImageVector by lazy {
        make("BarChart",
            "M4 20h16",
            "M7 20v-7M12 20V6M17 20v-10"
        )
    }

    val Calendar: ImageVector by lazy {
        make("Calendar",
            "M5 5h14a1 1 0 0 1 1 1v13a1 1 0 0 1-1 1H5a1 1 0 0 1-1-1V6a1 1 0 0 1 1-1z",
            "M4 10h16M8 3v4M16 3v4"
        )
    }

    val Sparkle: ImageVector by lazy {
        make("Sparkle",
            "M4 6l2 2M12 2v3M20 6l-2 2"
        )
    }
}

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
                strokeLineWidth = 1.8f,
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
            "M6 3h12v18l-3-2-3 2-3-2-3 2z",
            "M9 8h6M9 12h6"
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
            "M8 4H16a3 3 0 0 1 3 3V14a3 3 0 0 1 -3 3H8a3 3 0 0 1 -3 -3V7a3 3 0 0 1 3 -3z",
            "M5 11h14M8 20v-3M16 20v-3M8.5 14h.01M15.5 14h.01"
        )
    }

    val Bag: ImageVector by lazy {
        make("Bag",
            "M6 8h12l1 12H5z",
            "M9 8a3 3 0 0 1 6 0"
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
            "M5 8h11v6a4 4 0 0 1-4 4H9a4 4 0 0 1-4-4z",
            "M16 10h2a2 2 0 0 1 0 4h-2M8 3v2M12 3v2"
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
            "M4 5a2 2 0 0 1 2-2h13v16H6a2 2 0 0 0-2 2z",
            "M4 19V5M9 8h6"
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
            "M9.5 2.5H14.5a2.5 2.5 0 0 1 2.5 2.5V19a2.5 2.5 0 0 1 -2.5 2.5H9.5a2.5 2.5 0 0 1 -2.5 -2.5V5a2.5 2.5 0 0 1 2.5 -2.5z",
            "M11 18.5h2"
        )
    }

    val Plane: ImageVector by lazy {
        make("Plane",
            "M21 4L3 11l7 2.5L12.5 21 21 4z",
            "M10 13.5L21 4"
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
            "M11 12a1 1 0 1 0 2 0a1 1 0 1 0 -2 0z"
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
            "M4 11V9a3 3 0 0 1 3-3h11l-3-3M20 13v2a3 3 0 0 1-3 3H6l3 3"
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
            "M7.5 11H16.5a2.5 2.5 0 0 1 2.5 2.5V18.5a2.5 2.5 0 0 1 -2.5 2.5H7.5a2.5 2.5 0 0 1 -2.5 -2.5V13.5a2.5 2.5 0 0 1 2.5 -2.5z",
            "M8 11V8a4 4 0 0 1 8 0v3"
        )
    }
}

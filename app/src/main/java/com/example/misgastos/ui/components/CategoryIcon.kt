package com.example.misgastos.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.misgastos.R

data class CategoryIconOption(
    val key: String,
    val labelRes: Int,
    val imageVector: ImageVector
)

object CategoryIcons {
    const val DEFAULT_KEY = "Category"

    val options = listOf(
        CategoryIconOption(DEFAULT_KEY, R.string.category_icon_category, Icons.Default.Category),
        CategoryIconOption("Home", R.string.category_icon_home, Icons.Default.Home),
        CategoryIconOption("Restaurant", R.string.category_icon_restaurant, Icons.Default.Restaurant),
        CategoryIconOption("Person", R.string.category_icon_person, Icons.Default.Person),
        CategoryIconOption("Receipt", R.string.category_icon_receipt, Icons.Default.Receipt),
        CategoryIconOption("ShoppingCart", R.string.category_icon_shopping_cart, Icons.Default.ShoppingCart),
        CategoryIconOption("Payments", R.string.category_icon_payments, Icons.Default.Payments),
        CategoryIconOption("Work", R.string.category_icon_work, Icons.Default.Work),
        CategoryIconOption("CreditCard", R.string.category_icon_credit_card, Icons.Default.CreditCard),
        CategoryIconOption("DirectionsCar", R.string.category_icon_directions_car, Icons.Default.DirectionsCar),
        CategoryIconOption("Favorite", R.string.category_icon_favorite, Icons.Default.Favorite),
        CategoryIconOption("School", R.string.category_icon_school, Icons.Default.School),
        CategoryIconOption("Help", R.string.category_icon_help, Icons.AutoMirrored.Filled.Help),
        CategoryIconOption("MoreHoriz", R.string.category_icon_more, Icons.Default.MoreHoriz)
    )

    fun vectorFor(key: String): ImageVector =
        options.firstOrNull { it.key == key }?.imageVector ?: Icons.Default.Category
}

@Composable
fun CategoryIcon(
    iconName: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current
) {
    Icon(
        imageVector = CategoryIcons.vectorFor(iconName),
        contentDescription = contentDescription,
        modifier = modifier,
        tint = tint
    )
}

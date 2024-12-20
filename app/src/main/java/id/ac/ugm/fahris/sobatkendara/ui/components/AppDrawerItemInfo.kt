package id.ac.ugm.fahris.sobatkendara.ui.components

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

data class AppDrawerItemInfo<T>(
    val drawerOption: T,
    @StringRes val title: Int,
    @DrawableRes val drawableId: Int,
    @StringRes val descriptionId: Int
)
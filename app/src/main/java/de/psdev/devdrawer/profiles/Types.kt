package de.psdev.devdrawer.profiles

import de.psdev.devdrawer.appwidget.AppInfo
import de.psdev.devdrawer.database.PackageFilter
import de.psdev.devdrawer.database.WidgetProfile

typealias WidgetActionListener = (WidgetProfile) -> Unit
typealias AppInfoActionListener = (AppInfo) -> Unit
typealias PackageFilterActionListener = (PackageFilter) -> Unit
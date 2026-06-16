package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import com.example.model.AlertParameter
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.Mountain
import com.example.model.MountainRepository
import com.example.model.RegionType
import com.example.ui.theme.*
import com.example.viewmodel.DailyWeatherDay
import com.example.viewmodel.ElevationWeather
import com.example.viewmodel.HourlyWeatherHour
import com.example.viewmodel.WeatherUiState
import com.example.viewmodel.WeatherViewModel
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherScreen(
    viewModel: WeatherViewModel,
    modifier: Modifier = Modifier
) {
    val selectedMountain by viewModel.selectedMountain.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    // Microclimate selector (0: Base Camp, 1: High Camp, 2: Summit)
    var selectedZoneIndex by remember { mutableStateOf(2) }

    // Always keep dynamic filter of mountain repository
    val filteredMountains = remember(searchQuery) {
        if (searchQuery.isBlank()) {
            MountainRepository.mountains
        } else {
            MountainRepository.mountains.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                        it.range.contains(searchQuery, ignoreCase = true) ||
                        it.country.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    // Base Scaffold
    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(MtnSlateDark),
        containerColor = MtnSlateDark,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MtnSlateDark)
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                // Application Branding Row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    // Soft-blue rounded brand badge
                    Box(
                        modifier = Modifier
                            .background(BentoBlueBg, RoundedCornerShape(14.dp))
                            .padding(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Landscape,
                            contentDescription = "Mountain Logo",
                            tint = BentoBlueIconBg,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "SUMMIT DIRECT",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.SansSerif,
                            color = BentoTextPrimary,
                            letterSpacing = 1.sp,
                            lineHeight = 20.sp
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "Location Pin",
                                tint = BentoBlueIconBg,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${selectedMountain.name}, ${selectedMountain.country}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = BentoTextSecondary
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .background(BentoPurpleBg, RoundedCornerShape(12.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "METEO-NET",
                            fontSize = 10.sp,
                            color = BentoPurpleText,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                // Elegant Search Field
                TextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(
                            "Search range, country, or peak...",
                            color = TextSecondary.copy(alpha = 0.7f),
                            fontSize = 14.sp
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = TextSecondary
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear Search",
                                    tint = TextSecondary
                                )
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .border(1.dp, MtnBorder, RoundedCornerShape(14.dp))
                        .testTag("mountain_search_input"),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MtnSlateCard,
                        unfocusedContainerColor = MtnSlateCard,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        cursorColor = AlpenglowRose,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() })
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Horizontal Capsule List of filtrated peaks
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp, horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(end = 16.dp)
            ) {
                items(filteredMountains) { mountain ->
                    val isSelected = mountain == selectedMountain
                    val borderAlpha by animateFloatAsState(if (isSelected) 1f else 0f, label = "borderAlpha")
                    val cardBgColor = if (isSelected) MtnSlateCardLighter else MtnSlateCard

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(cardBgColor, RoundedCornerShape(20.dp))
                            .border(
                                width = 1.dp,
                                color = if (isSelected) AlpenglowRose.copy(alpha = borderAlpha) else MtnBorder,
                                shape = RoundedCornerShape(20.dp)
                            )
                            .clickable {
                                viewModel.selectMountain(mountain)
                                selectedZoneIndex = 2 // Always reset to Summit view
                            }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                            .testTag("mountain_capsule_${mountain.name.lowercase().replace(" ", "_")}")
                    ) {
                        Icon(
                            imageVector = when (mountain.regionType) {
                                RegionType.VOLCANO -> Icons.Default.LocalFireDepartment
                                RegionType.POLAR -> Icons.Default.AcUnit
                                else -> Icons.Default.Terrain
                            },
                            contentDescription = null,
                            tint = if (isSelected) AlpenglowRose else TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = mountain.name,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) AlpenglowRose else TextPrimary
                            )
                            Text(
                                text = "${mountain.elevationMeters}m · ${mountain.range}",
                                fontSize = 10.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }

                if (filteredMountains.isEmpty()) {
                    item {
                        Text(
                            text = "No peaks correspond to queries",
                            color = TextMuted,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }

            // Primary Content Stream
            Crossfade(
                targetState = uiState,
                animationSpec = tween(durationMillis = 400),
                label = "state_crossfade"
            ) { state ->
                when (state) {
                    is WeatherUiState.Loading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(color = AlpenglowRose)
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Acquiring localized telemetry...",
                                    fontSize = 14.sp,
                                    color = TextSecondary,
                                    letterSpacing = 1.sp
                                )
                            }
                        }
                    }

                    is WeatherUiState.Success -> {
                        MainForecastLayout(
                            viewModel = viewModel,
                            selectedMountain = selectedMountain,
                            state = state,
                            selectedZoneIndex = selectedZoneIndex,
                            onZoneSelected = { selectedZoneIndex = it }
                        )
                    }

                    is WeatherUiState.Error -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .weight(1f)
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .background(MtnSlateCard, RoundedCornerShape(16.dp))
                                    .border(1.dp, AlpenglowRose.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                                    .padding(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = "Error",
                                    tint = AlpenglowRose,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Error Loading Station Telemetry",
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary,
                                    fontSize = 16.sp
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = state.message,
                                    color = TextSecondary,
                                    fontSize = 13.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MainForecastLayout(
    viewModel: WeatherViewModel,
    selectedMountain: Mountain,
    state: WeatherUiState.Success,
    selectedZoneIndex: Int,
    onZoneSelected: (Int) -> Unit
) {
    val activeZone = remember(state.elevations, selectedZoneIndex) {
        state.elevations.getOrNull(selectedZoneIndex) ?: state.elevations.last()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("forecast_scroll_column"),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section: Interactive Kinetic Graphics Canvas
        item {
            KineticMountainGraphic(
                mountain = selectedMountain,
                activeZoneWeather = activeZone,
                elevations = state.elevations,
                selectedZoneIndex = selectedZoneIndex,
                onZoneSelected = onZoneSelected,
                isSimulated = state.isSimulated
            )
        }

        // Section: Primary Digital Altitude HUD telemetry cards
        item {
            ZoneTelemetryGrid(zoneWeather = activeZone)
        }

        // Section: 24h Hourly Trend curve
        item {
            HourlyTimeline(hourlyHours = state.hourly)
        }

        // Section: 7-Day Outlook
        item {
            WeeklyOutlook(dailyDays = state.daily)
        }

        // Section: Customizable Notification Alerts Bento Card
        item {
            CustomAlertPanel(viewModel = viewModel)
        }
    }
}

@Composable
fun KineticMountainGraphic(
    mountain: Mountain,
    activeZoneWeather: ElevationWeather,
    elevations: List<ElevationWeather>,
    selectedZoneIndex: Int,
    onZoneSelected: (Int) -> Unit,
    isSimulated: Boolean
) {
    // Standard game loop ticker simulation inside Compose
    val infiniteTransition = rememberInfiniteTransition(label = "mountain_loop")
    val ticker by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ticking"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(BentoBlueGradientStart, BentoBlueGradientEnd)
                ),
                shape = RoundedCornerShape(32.dp)
            )
            .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(32.dp))
            .clip(RoundedCornerShape(32.dp))
    ) {
        // Header Mountain Title HUD overlay
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = mountain.name.uppercase(),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp,
                        color = Color.White,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .background(
                                color = Color.White.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = mountain.difficulty,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
                Text(
                    text = "${mountain.range} Range · ${mountain.country}",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Real-Time versus Science-Simulated marker badge
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .background(
                        Color.Black.copy(alpha = 0.2f),
                        RoundedCornerShape(10.dp)
                    )
                    .border(
                        1.dp,
                        Color.White.copy(alpha = 0.25f),
                        RoundedCornerShape(10.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(
                                color = if (isSimulated) SummitGold else MeadowGreen,
                                shape = androidx.compose.foundation.shape.CircleShape
                            )
                    )
                    Text(
                        text = if (isSimulated) "Simulated Model" else "Telemetry Live",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }

        // Animated weather-scape canvas element featuring authentic mountain drawing
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(Color.Transparent) // Blend seamlessly with bento parent gradient background
        ) {
            // Live particles rendering inside backing canvas modifier
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("mountain_custom_canvas")
            ) {
                val width = size.width
                val height = size.height

                // Draw atmospheric glow/sun beams if clear sky
                val code = activeZoneWeather.weatherCode
                if (code == 0 || code == 1) {
                    val sunRadius = 24.dp.toPx()
                    val sunCenter = Offset(width * 0.85f, height * 0.25f)
                    
                    // Draw pulse rays
                    for (i in 0 until 8) {
                        val angle = (i * Math.PI / 4) + (ticker * 2 * Math.PI / 8)
                        val start = Offset(
                            (sunCenter.x + sunRadius * 1.2 * cos(angle)).toFloat(),
                            (sunCenter.y + sunRadius * 1.2 * sin(angle)).toFloat()
                        )
                        val end = Offset(
                            (sunCenter.x + sunRadius * 1.8 * cos(angle)).toFloat(),
                            (sunCenter.y + sunRadius * 1.8 * sin(angle)).toFloat()
                        )
                        drawLine(
                            color = SummitGold.copy(alpha = 0.3f + 0.15f * sin((ticker * 2 * Math.PI).toFloat())),
                            start = start,
                            end = end,
                            strokeWidth = 2.dp.toPx()
                        )
                    }

                    drawCircle(
                        color = SummitGold.copy(alpha = 0.8f),
                        radius = sunRadius,
                        center = sunCenter
                    )
                }

                // Draw heavy clouds if weather code is cloudy (2, 3, 45, 48)
                if (code in listOf(2, 3, 45, 48, 51, 53, 55, 61, 63, 65, 71, 73, 75, 77, 85, 86, 95, 96, 99)) {
                    val cloudBrush = Brush.linearGradient(
                        colors = listOf(Color(0xFF4B5E78).copy(alpha = 0.45f), Color(0xFF1E2638).copy(alpha = 0.25f))
                    )
                    
                    // Main Cloud blobs
                    drawCircle(cloudBrush, radius = 50.dp.toPx(), center = Offset(width * 0.2f + 10 * sin(ticker * 2 * Math.PI).toFloat(), height * 0.25f))
                    drawCircle(cloudBrush, radius = 60.dp.toPx(), center = Offset(width * 0.35f + 15 * sin(ticker * 2 * Math.PI).toFloat(), height * 0.2f))
                    drawCircle(cloudBrush, radius = 45.dp.toPx(), center = Offset(width * 0.48f + 8 * sin(ticker * 2 * Math.PI).toFloat(), height * 0.28f))
                    
                    if (code in listOf(95, 96, 99, 75, 86)) { // extra dark storm clouds
                        val stormBrush = Brush.linearGradient(
                            colors = listOf(Color(0xFF1A2234).copy(alpha = 0.5f), Color(0xFF0F1522).copy(alpha = 0.3f))
                        )
                        drawCircle(stormBrush, radius = 70.dp.toPx(), center = Offset(width * 0.75f - 12 * Math.cos(ticker * 2 * Math.PI).toFloat(), height * 0.18f))
                        drawCircle(stormBrush, radius = 55.dp.toPx(), center = Offset(width * 0.88f - 8 * Math.cos(ticker * 2 * Math.PI).toFloat(), height * 0.24f))
                    }
                }

                // Draw Mountain Silhouette
                val baseLeft = Offset(-50f, height + 10f)
                val baseRight = Offset(width + 50f, height + 10f)
                val peakCenter = Offset(width * 0.5f, height * 0.28f)

                val mountainPath = Path().apply {
                    moveTo(baseLeft.x, baseLeft.y)
                    // Ridge left details
                    lineTo(width * 0.25f, height * 0.65f)
                    lineTo(width * 0.35f, height * 0.52f)
                    lineTo(peakCenter.x, peakCenter.y)
                    // Ridge right details
                    lineTo(width * 0.68f, height * 0.58f)
                    lineTo(width * 0.80f, height * 0.72f)
                    lineTo(baseRight.x, baseRight.y)
                    close()
                }

                // Mountain base color gradient: deep navy blending down cohesively
                drawPath(
                    path = mountainPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF1E3A5F).copy(alpha = 0.95f),
                            Color(0xFF001D36)
                        )
                    )
                )

                // Draw Snow Cap at top 35% of the mountain
                val capPeakLeft = Offset(width * 0.4f, height * 0.48f)
                val capPeakRight = Offset(width * 0.60f, height * 0.51f)
                val snowCapPath = Path().apply {
                    moveTo(capPeakLeft.x, capPeakLeft.y)
                    lineTo(width * 0.44f, height * 0.55f)
                    lineTo(width * 0.48f, height * 0.51f)
                    lineTo(width * 0.52f, height * 0.56f)
                    lineTo(width * 0.56f, height * 0.52f)
                    lineTo(capPeakRight.x, capPeakRight.y)
                    lineTo(peakCenter.x, peakCenter.y)
                    close()
                }

                // Beautiful glowing glacier snowcap with reflected pink/sky tints
                drawPath(
                    path = snowCapPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White,
                            Color.White.copy(alpha = 0.9f),
                            Color(0xFFD1E4FF).copy(alpha = 0.5f)
                        )
                    )
                )

                // Draw outline for clean vector aesthetic
                drawPath(
                    path = mountainPath,
                    color = Color.White.copy(alpha = 0.25f),
                    style = Stroke(width = 1.5.dp.toPx())
                )

                // Draw Wind force lines (moving horizontal waves)
                val windKmh = activeZoneWeather.windSpeedKmh
                if (windKmh > 10.0) {
                    val strokeW = if (windKmh > 40) 2.dp.toPx() else 1.dp.toPx()
                    val windAlpha = (windKmh / 120.0).coerceIn(0.15, 0.7).toFloat()
                    val strokeColor = Color(0xFFD1E4FF).copy(alpha = windAlpha)

                    // Wave form calculations offset by ticker
                    drawWindTrail(width, height, ticker, strokeColor, strokeW, yPos = height * 0.45f, freq = 0.02f, amp = 15f)
                    drawWindTrail(width, height, ticker + 0.3f, strokeColor, strokeW * 1.5f, yPos = height * 0.35f, freq = 0.03f, amp = 20f)
                    drawWindTrail(width, height, ticker + 0.6f, strokeColor, strokeW, yPos = height * 0.65f, freq = 0.015f, amp = 10f)
                }

                // Particle generators based on weather factors (rain, snow, blizzard)
                if (code in listOf(51, 53, 55, 61, 63, 65, 80, 81, 82, 95, 96, 99)) {
                    val rCount = 28
                    val windSkew = (windKmh / 5).coerceIn(2.0, 15.0).toFloat()
                    for (i in 0 until rCount) {
                        val startX = (i * 35.8f + ticker * 150) % width
                        val startY = (i * 22.4f + ticker * height * 1.8f) % height
                        drawLine(
                            color = Color(0xFFD1E4FF).copy(alpha = 0.6f),
                            start = Offset(startX, startY),
                            end = Offset(startX - windSkew, startY + 12.dp.toPx()),
                            strokeWidth = 1.5.dp.toPx()
                        )
                    }
                } else if (code in listOf(71, 73, 75, 77, 85, 86)) {
                    val sCount = 35
                    val driftSkew = (windKmh / 3).coerceIn(1.0, 18.0).toFloat()
                    for (i in 0 until sCount) {
                        val startX = (i * 41.5f + ticker * driftSkew * 2) % width
                        val startY = (i * 18.2f + ticker * height * 1.1f) % height
                        val radius = if (i % 3 == 0) 3.5.dp.toPx() else 2.dp.toPx()
                        drawCircle(
                            color = Color.White.copy(alpha = 0.8f),
                            radius = radius,
                            center = Offset(startX, startY)
                        )
                    }
                }
            }

            // Temperature HUD sticker overlay (centered in bottom right zone)
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
                    .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                    .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                    .padding(10.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "TEMPERATURE",
                        fontSize = 8.sp,
                        color = Color.White.copy(alpha = 0.7f),
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "${activeZoneWeather.temperatureCelsius.toInt()}°C",
                        fontSize = 24.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "Chill ${activeZoneWeather.apparentTemperatureCelsius.toInt()}°C",
                        fontSize = 11.sp,
                        color = if (activeZoneWeather.apparentTemperatureCelsius < 0) Color(0xFF90CAF9) else Color(0xFFFFCC80),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Elevation Level Indicators & Interactive Nodes directly over the mountain
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 12.dp)
            ) {
                val zones = listOf("Summit", "High Camp", "Base Camp")
                Column(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    elevations.reversed().forEachIndexed { index, el ->
                        val actualIndex = 2 - index
                        val active = actualIndex == selectedZoneIndex

                        val backgroundPulse by animateColorAsState(
                            targetValue = if (active) Color.White else Color.Black.copy(alpha = 0.35f),
                            label = "pill_bg"
                        )
                        val borderCol by animateColorAsState(
                            targetValue = if (active) Color.White else Color.White.copy(alpha = 0.15f),
                            label = "pill_border"
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .background(backgroundPulse, RoundedCornerShape(10.dp))
                                .border(1.dp, borderCol, RoundedCornerShape(10.dp))
                                .clickable { onZoneSelected(actualIndex) }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                .testTag("elevation_zone_${el.name.lowercase().replace(" ", "_")}")
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(
                                        color = if (active) BentoBlueIconBg else Color.White.copy(alpha = 0.6f),
                                        shape = androidx.compose.foundation.shape.CircleShape
                                    )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${el.name} · ${el.elevationMeters}m",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (active) BentoBlueText else Color.White.copy(alpha = 0.9f)
                            )
                        }
                    }
                }
            }
        }

        // Zone description status text below in semi-translucent glass bottom bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.12f))
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Details",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = when (selectedZoneIndex) {
                        2 -> "Summit level: Extreme winds, high UV exposure, severely reduced oxygen density."
                        1 -> "High camp level: Advanced snow fields, high risks of sudden wind gusts, stable subzero cold."
                        else -> "Base station: Denser valley atmosphere, warmer alpine temperatures, baseline metrics."
                    },
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.95f),
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

private fun DrawScope.drawWindTrail(
    width: Float,
    height: Float,
    ticker: Float,
    color: Color,
    strokeW: Float,
    yPos: Float,
    freq: Float,
    amp: Float
) {
    val windPath = Path()
    var joined = false
    
    // Wave moves left to right, parameterized by ticket cycle
    val slideOffset = ticker * width
    for (x in 0..width.toInt() step 8) {
        val calculatedX = x.toFloat()
        // Wave math
        val relativeOffsetVal = (slideOffset + calculatedX) * freq
        val waveY = yPos + amp * sin(relativeOffsetVal).toFloat()

        if (!joined) {
            windPath.moveTo(calculatedX, waveY)
            joined = true
        } else {
            windPath.lineTo(calculatedX, waveY)
        }
    }
    drawPath(
        path = windPath,
        color = color,
        style = Stroke(width = strokeW, cap = StrokeCap.Round)
    )
}

@Composable
fun ZoneTelemetryGrid(zoneWeather: ElevationWeather) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = "ALTITUDE TELEMETRY DATA",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = AlpenglowRose,
            letterSpacing = 1.5.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Wind Velocity dial
            TelemetryCard(
                title = "WIND VELOCITY",
                value = "${zoneWeather.windSpeedKmh.toInt()} km/h",
                subText = getWindWarningRating(zoneWeather.windSpeedKmh),
                icon = Icons.Default.Air,
                iconColor = GlacialSky,
                modifier = Modifier.weight(1f)
            )

            // Oxygen Density relative to standard Sea level density
            TelemetryCard(
                title = "OXYGEN DENSITY",
                value = "${zoneWeather.oxygenPercentage}%",
                subText = getO2WarningRating(zoneWeather.oxygenPercentage),
                icon = Icons.Default.Air, // substitute
                iconColor = AlpenglowRose,
                modifier = Modifier.weight(1f),
                showO2Progress = true,
                o2ProgressValue = zoneWeather.oxygenPercentage / 100f
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Barometric Pressure
            TelemetryCard(
                title = "BAROM. PRESSURE",
                value = "${zoneWeather.pressureHpa.toInt()} hPa",
                subText = "Elev: ${zoneWeather.elevationMeters}m",
                icon = Icons.Outlined.Speed,
                iconColor = SummitGold,
                modifier = Modifier.weight(1f)
            )

            // Weather Code Condition mapped description
            TelemetryCard(
                title = "CLIMATE STATE",
                value = getWeatherConditionString(zoneWeather.weatherCode),
                subText = "WMO Code ${zoneWeather.weatherCode}",
                icon = getWeatherConditionIcon(zoneWeather.weatherCode),
                iconColor = MeadowGreen,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun TelemetryCard(
    title: String,
    value: String,
    subText: String,
    icon: ImageVector,
    iconColor: Color,
    modifier: Modifier = Modifier,
    showO2Progress: Boolean = false,
    o2ProgressValue: Float = 1f
) {
    // Determine the styling based on the title
    val isWind = title.contains("WIND", ignoreCase = true)
    val isO2 = title.contains("OXYGEN", ignoreCase = true)
    val isRisk = title.contains("CLIMATE", ignoreCase = true) || title.contains("RISK", ignoreCase = true)

    val containerBg: Color
    val textPrimaryColor: Color
    val textSecondaryColor: Color
    val iconBadgeBg: Color
    val iconTint: Color
    val cardBorder: BorderStroke

    if (isWind) {
        containerBg = BentoBlueBg // #D1E4FF
        textPrimaryColor = BentoBlueText // #001D36
        textSecondaryColor = BentoBlueText.copy(alpha = 0.7f)
        iconBadgeBg = BentoBlueIconBg // #005FB0
        iconTint = Color.White
        cardBorder = BorderStroke(1.dp, Color(0xFFB0D0FF))
    } else if (isO2) {
        containerBg = BentoPurpleBg // #E8DEF8
        textPrimaryColor = BentoPurpleText // #21005D
        textSecondaryColor = BentoPurpleText.copy(alpha = 0.7f)
        iconBadgeBg = Color.White.copy(alpha = 0.6f)
        iconTint = BentoPurpleText
        cardBorder = BorderStroke(1.dp, BentoPurpleBorder)
    } else if (isRisk) {
        // Classically styled alert state with warning badge
        containerBg = BentoCardWhite // #FFFFFF
        textPrimaryColor = BentoTextPrimary // #1B1B1F
        textSecondaryColor = BentoRedIconBg // #BA1A1A (or BentoTextSecondary)
        iconBadgeBg = BentoRedIconBg // #BA1A1A
        iconTint = Color.White
        cardBorder = BorderStroke(1.dp, BentoBorder)
    } else {
        // Barometric Pressure / Standard white card
        containerBg = BentoCardWhite
        textPrimaryColor = BentoTextPrimary
        textSecondaryColor = BentoTextSecondary
        iconBadgeBg = BentoBlueBg
        iconTint = BentoBlueIconBg
        cardBorder = BorderStroke(1.dp, BentoBorder)
    }

    Card(
        modifier = modifier
            .testTag("telemetry_card_${title.lowercase().replace(" ", "_")}"),
        colors = CardDefaults.cardColors(
            containerColor = containerBg
        ),
        shape = RoundedCornerShape(24.dp), // Bento styling uses generous rounded corners 24dp
        border = cardBorder
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp) // Maintain consistent box height for a highly-aligned Bento system
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // TOP row: Icon Badge on LEFT, Card Title on RIGHT
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Round/Square Icon Badge with background
                Box(
                    modifier = Modifier
                        .background(iconBadgeBg, RoundedCornerShape(12.dp))
                        .padding(6.dp)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Text(
                    text = title.replace("VELOCITY", "").replace("PRESSURE", "").trim(),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    color = textPrimaryColor.copy(alpha = 0.45f),
                    letterSpacing = 1.sp,
                    textAlign = TextAlign.End
                )
            }

            // BOTTOM section: Massive value and explanatory sub-status
            Column {
                Text(
                    text = value,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = textPrimaryColor,
                    lineHeight = 22.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                
                if (showO2Progress) {
                    LinearProgressIndicator(
                        progress = { o2ProgressValue },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = textPrimaryColor,
                        trackColor = textPrimaryColor.copy(alpha = 0.15f),
                    )
                }

                Text(
                    text = subText,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = textSecondaryColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun HourlyTimeline(hourlyHours: List<HourlyWeatherHour>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = "24-HOUR HOURLY TEMPERATURE PATH",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = AlpenglowRose,
            letterSpacing = 1.5.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .background(MtnSlateCard, RoundedCornerShape(20.dp))
                .border(1.dp, MtnBorder, RoundedCornerShape(20.dp))
                .padding(horizontal = 12.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(hourlyHours) { h ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.width(44.dp)
                ) {
                    Text(
                        text = h.hourLabel,
                        fontSize = 10.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Icon(
                        imageVector = getWeatherConditionIcon(h.weatherCode),
                        contentDescription = null,
                        tint = if (h.temp < 0) TempColdIce else SummitGold,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "${h.temp.toInt()}°",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun WeeklyOutlook(dailyDays: List<DailyWeatherDay>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = "7-DAY REGIONAL STATIONS OUTLOOK",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = AlpenglowRose,
            letterSpacing = 1.5.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MtnSlateCard, RoundedCornerShape(20.dp))
                .border(1.dp, MtnBorder, RoundedCornerShape(20.dp))
                .padding(vertical = 10.dp)
        ) {
            dailyDays.forEachIndexed { index, day ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = day.dateLabel,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.width(62.dp)
                    )

                    Icon(
                        imageVector = getWeatherConditionIcon(day.weatherCode),
                        contentDescription = null,
                        tint = if (day.tempMin < 0) TempColdIce else SummitGold,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = getWeatherConditionString(day.weatherCode),
                        fontSize = 11.sp,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = "UV ${day.uvIndexMax.toInt()}  · ",
                        fontSize = 10.sp,
                        color = TextMuted
                    )

                    Text(
                        text = "🌪️ ${day.windSpeedMaxKmh.toInt()} km/h  · ",
                        fontSize = 10.sp,
                        color = TextMuted
                    )

                    Row(
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier.width(66.dp)
                    ) {
                        Text(
                            text = "${day.tempMax.toInt()}°",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${day.tempMin.toInt()}°",
                            fontSize = 13.sp,
                            color = TextSecondary
                        )
                    }
                }

                if (index < dailyDays.size - 1) {
                    HorizontalDivider(
                        color = MtnBorder.copy(alpha = 0.5f),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
        }
    }
}

// Helpers meteorological mappings
private fun getWindWarningRating(windSpeedKmh: Double): String {
    return when {
        windSpeedKmh >= 90 -> "Extreme Gale (Risk)"
        windSpeedKmh >= 60 -> "Severe Gale Storm"
        windSpeedKmh >= 35 -> "Moderate Alpine Gale"
        else -> "Calm Valley Breezes"
    }
}

private fun getO2WarningRating(o2Pct: Int): String {
    return when {
        o2Pct <= 35 -> "Death Zone (Bottled)"
        o2Pct <= 55 -> "Extremely Thin Air"
        o2Pct <= 75 -> "Hypoxia Warning"
        else -> "Safe Acclimatized"
    }
}

private fun getWeatherConditionString(code: Int): String {
    return when (code) {
        0 -> "Clear Sky"
        1 -> "Mainly Clear"
        2 -> "Partly Cloudy"
        3 -> "Overcast Sky"
        45, 48 -> "Dense Alpine Fog"
        51, 53, 55 -> "Misty Drizzle"
        61, 63, 65 -> "Alpine Rain"
        71, 73 -> "Lighter Snowfall"
        75 -> "Heavy Glacier Blizzard"
        77 -> "Sleet / Snow Grains"
        80, 81, 82 -> "Rain Showers"
        85, 86 -> "Torrential Snowfall"
        95 -> "Severe Snowstorms"
        96, 99 -> "Extreme Blizzard"
        else -> "Alpine Condition"
    }
}

private fun getWeatherConditionIcon(code: Int): ImageVector {
    return when (code) {
        0, 1 -> Icons.Filled.WbSunny
        2, 3 -> Icons.Filled.Cloud
        45, 48 -> Icons.Filled.Grain
        51, 53, 55 -> Icons.Outlined.WaterDrop
        61, 63, 65 -> Icons.Default.WaterDrop
        71, 73, 75, 77 -> Icons.Default.AcUnit
        80, 81, 82 -> Icons.Filled.Cloud
        85, 86 -> Icons.Default.AcUnit
        95, 96, 99 -> Icons.Default.Thunderstorm
        else -> Icons.Default.Cloud
    }
}

@Composable
fun CustomAlertPanel(
    viewModel: WeatherViewModel,
    modifier: Modifier = Modifier
) {
    val alerts by viewModel.alerts.collectAsStateWithLifecycle()
    val selectedMountain by viewModel.selectedMountain.collectAsStateWithLifecycle()

    var expandAddRule by remember { mutableStateOf(false) }
    var selectedMtnName by remember { mutableStateOf(selectedMountain.name) }
    var selectedParam by remember { mutableStateOf(AlertParameter.HIGH_WINDS) }
    var customThreshold by remember { mutableStateOf(50f) }

    LaunchedEffect(selectedMountain) {
        selectedMtnName = selectedMountain.name
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .testTag("weather_alerts_panel_card"),
        colors = CardDefaults.cardColors(containerColor = BentoCardWhite),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, BentoBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .background(BentoPurpleBg, RoundedCornerShape(12.dp))
                            .padding(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = "Alerts",
                            tint = BentoPurpleText,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "CRITERIA ALERTS",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            color = BentoTextPrimary,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "${alerts.filter { it.isEnabled }.size} active telemetry rules",
                            fontSize = 11.sp,
                            color = BentoTextSecondary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                IconButton(
                    onClick = { expandAddRule = !expandAddRule },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = if (expandAddRule) BentoPurpleBg else BentoBg,
                        contentColor = BentoPurpleText
                    )
                ) {
                    Icon(
                        imageVector = if (expandAddRule) Icons.Default.Close else Icons.Default.Add,
                        contentDescription = "Toggle add rule drawer"
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            AnimatedVisibility(
                visible = expandAddRule,
                enter = expandIn() + fadeIn(),
                exit = shrinkOut() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(BentoBg, RoundedCornerShape(18.dp))
                        .border(1.dp, BentoBorder, RoundedCornerShape(18.dp))
                        .padding(14.dp)
                ) {
                    Text(
                        text = "NEW TELEMETRY TRIGGER RULE",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = BentoTextMuted,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Select Mountain Peak",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = BentoTextSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(vertical = 2.dp)
                    ) {
                        items(MountainRepository.mountains) { mtn ->
                            val isSelected = selectedMtnName == mtn.name
                            Box(
                                modifier = Modifier
                                    .background(
                                        color = if (isSelected) BentoBlueBg else Color.White,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = if (isSelected) BentoBlueIconBg.copy(alpha = 0.5f) else BentoBorder,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable { selectedMtnName = mtn.name }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                                    .testTag("rule_mtn_choice_${mtn.name.lowercase().replace(" ", "_")}")
                            ) {
                                Text(
                                    text = mtn.name,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                                    color = if (isSelected) BentoBlueText else BentoTextPrimary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Breach Parameter",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = BentoTextSecondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        AlertParameter.values().forEach { param ->
                            val isSelected = selectedParam == param
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(
                                        color = if (isSelected) BentoPurpleBg else Color.White,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = if (isSelected) BentoPurpleText.copy(alpha = 0.5f) else BentoBorder,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable {
                                        selectedParam = param
                                        customThreshold = param.defaultThreshold.toFloat()
                                    }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = when (param) {
                                            AlertParameter.SNOWFALL -> Icons.Default.AcUnit
                                            AlertParameter.HIGH_WINDS -> Icons.Default.Air
                                            AlertParameter.STORMS -> Icons.Default.Thunderstorm
                                            AlertParameter.EXTREME_COLD -> Icons.Default.AcUnit
                                        },
                                        contentDescription = null,
                                        tint = if (isSelected) BentoPurpleText else BentoTextSecondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = param.displayName.substringBefore(" "),
                                        fontSize = 10.sp,
                                        fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium,
                                        color = if (isSelected) BentoPurpleText else BentoTextPrimary
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (selectedParam == AlertParameter.HIGH_WINDS || selectedParam == AlertParameter.EXTREME_COLD) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Threshold Specification",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = BentoTextSecondary
                            )
                            Text(
                                text = if (selectedParam == AlertParameter.HIGH_WINDS) {
                                    ">= ${customThreshold.toInt()} km/h"
                                } else {
                                    "<= ${customThreshold.toInt()} °C"
                                },
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                color = BentoPurpleText
                            )
                        }
                        
                        Slider(
                            value = customThreshold,
                            onValueChange = { customThreshold = it },
                            valueRange = if (selectedParam == AlertParameter.HIGH_WINDS) {
                                10f..120f
                            } else {
                                -40f..5f
                            },
                            colors = SliderDefaults.colors(
                                thumbColor = BentoPurpleText,
                                activeTrackColor = BentoPurpleText,
                                inactiveTrackColor = BentoBorder
                            ),
                            modifier = Modifier.testTag("rule_threshold_slider")
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White, RoundedCornerShape(10.dp))
                                .border(1.dp, BentoBorder, RoundedCornerShape(10.dp))
                                .padding(10.dp)
                        ) {
                            Text(
                                text = "💡 This rule triggers automatically whenever satellite telemetry registers active blizzard snowfall or lightning storm conditions at any mountain camp level.",
                                fontSize = 11.sp,
                                color = BentoTextSecondary,
                                lineHeight = 14.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            viewModel.addAlert(selectedMtnName, selectedParam, customThreshold.toDouble())
                            expandAddRule = false
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BentoPurpleText,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("activate_alert_rule_button")
                    ) {
                        Text(
                            text = "ACTIVATE TELEMETRY RULE",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }

            if (expandAddRule) {
                Spacer(modifier = Modifier.height(12.dp))
            }

            if (alerts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No custom criteria alerts active.",
                        fontSize = 12.sp,
                        color = BentoTextMuted,
                        fontWeight = FontWeight.Medium
                    )
                }
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    alerts.forEach { alert ->
                        val isTriggeredAndActive = alert.isEnabled && alert.isTriggered
                        val currentBorder = if (isTriggeredAndActive) {
                            BorderStroke(1.2.dp, BentoRedIconBg)
                        } else {
                            BorderStroke(1.dp, BentoBorder)
                        }
                        
                        val backgroundAlpha = if (isTriggeredAndActive) BentoRedBg else Color.White

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(backgroundAlpha, RoundedCornerShape(16.dp))
                                .border(currentBorder, RoundedCornerShape(16.dp))
                                .padding(12.dp)
                                .testTag("alert_rule_item_${alert.id}"),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .background(
                                        color = if (isTriggeredAndActive) BentoRedIconBg else BentoBg,
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .padding(8.dp)
                            ) {
                                Icon(
                                    imageVector = when (alert.parameter) {
                                        AlertParameter.HIGH_WINDS -> Icons.Default.Air
                                        AlertParameter.EXTREME_COLD -> Icons.Default.AcUnit
                                        AlertParameter.SNOWFALL -> Icons.Default.AcUnit
                                        AlertParameter.STORMS -> Icons.Default.Thunderstorm
                                    },
                                    contentDescription = null,
                                    tint = if (isTriggeredAndActive) Color.White else BentoTextSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = alert.mountainName.uppercase(),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black,
                                        color = if (isTriggeredAndActive) BentoRedText else BentoTextPrimary
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    if (isTriggeredAndActive) {
                                        Box(
                                            modifier = Modifier
                                                .background(BentoRedIconBg, RoundedCornerShape(6.dp))
                                                .padding(horizontal = 4.dp, vertical = 1.dp)
                                        ) {
                                            Text(
                                                text = "BREACH",
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.Black,
                                                color = Color.White
                                            )
                                        }
                                    }
                                }

                                Text(
                                    text = when (alert.parameter) {
                                        AlertParameter.HIGH_WINDS -> "Winds exceeding ${alert.thresholdValue.toInt()} km/h"
                                        AlertParameter.EXTREME_COLD -> "Apparent cold below ${alert.thresholdValue.toInt()}°C"
                                        AlertParameter.SNOWFALL -> "Blizzard snow warning"
                                        AlertParameter.STORMS -> "Violent storm warning"
                                    },
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isTriggeredAndActive) BentoRedText.copy(alpha = 0.8f) else BentoTextSecondary
                                )

                                if (isTriggeredAndActive && !alert.lastTriggerDetail.isNullOrBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "🚨 ${alert.lastTriggerDetail}",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = BentoRedIconBg,
                                        lineHeight = 12.sp
                                    )
                                } else {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = if (alert.isEnabled) "Actively monitoring live telemetry feed..." else "Rule deactivated",
                                        fontSize = 9.sp,
                                        color = BentoTextMuted,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(4.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                if (alert.isEnabled) {
                                    IconButton(
                                        onClick = { viewModel.testTriggerAlert(alert) },
                                        modifier = Modifier.size(28.dp).testTag("alert_test_button_${alert.id}")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Notifications,
                                            contentDescription = "Test Notification Push",
                                            tint = BentoTextSecondary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }

                                Switch(
                                    checked = alert.isEnabled,
                                    onCheckedChange = { viewModel.toggleAlertEnabled(alert.id) },
                                    modifier = Modifier.scale(0.7f).testTag("alert_toggle_${alert.id}"),
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = BentoPurpleText,
                                        checkedTrackColor = BentoPurpleBg,
                                        uncheckedThumbColor = BentoTextMuted,
                                        uncheckedTrackColor = BentoBg
                                    )
                                )

                                IconButton(
                                    onClick = { viewModel.deleteAlert(alert.id) },
                                    modifier = Modifier.size(28.dp).testTag("alert_delete_${alert.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete Alert Rule",
                                        tint = BentoTextMuted,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Bento Grid Theme Colors
val BentoBg = Color(0xFFF3F4F9)           // Main Light Gray-blue background
val BentoCardWhite = Color(0xFFFFFFFF)     // Standard White Card background
val BentoBorder = Color(0xFFE1E2E9)        // Subtle card borders

// Bento Specific Card Backgrounds & Accents
val BentoBlueBg = Color(0xFFD1E4FF)       // Light blue card background (Wind card, active chips)
val BentoBlueText = Color(0xFF001D36)     // Deep blue text for blue card
val BentoBlueIconBg = Color(0xFF005FB0)   // Dark blue solid for icon badges
val BentoBlueGradientStart = Color(0xFF005FB0) // Main hero gradient start
val BentoBlueGradientEnd = Color(0xFF5196E1)   // Main hero gradient end

val BentoRedBg = Color(0xFFFFDAD9)        // Very light red for warning / danger sections
val BentoRedText = Color(0xFF410002)      // Deep red text
val BentoRedIconBg = Color(0xFFBA1A1A)     // Solid red for warning badge

val BentoPurpleBg = Color(0xFFE8DEF8)     // Soft lavender card background
val BentoPurpleText = Color(0xFF21005D)   // Deep purple text for lavender card
val BentoPurpleBorder = Color(0xFFE2D6F5)

// Typography Palette
val BentoTextPrimary = Color(0xFF1B1B1F)   // Core dark text
val BentoTextSecondary = Color(0xFF44474E) // Medium gray text descriptive
val BentoTextMuted = Color(0xFF74777F)     // Low-contrast caption text

// Retained alpenglow accents for dynamic highlights if needed
val AlpenglowRose = Color(0xFF005FB0)      // Map alpenglow to Bento primary blue
val GlacialSky = Color(0xFF5196E1)
val SummitGold = Color(0xFFFFB03A)
val MeadowGreen = Color(0xFF4EE4A7)
val TempColdIce = Color(0xFF008CCF)
val MistGray = Color(0xFF74777F)

// Classic Mountain Slate dark variables for backup or secondary states
val MtnSlateDark = BentoBg
val MtnSlateCard = BentoCardWhite
val MtnSlateCardLighter = BentoBlueBg
val MtnBorder = BentoBorder
val TextPrimary = BentoTextPrimary
val TextSecondary = BentoTextSecondary
val TextMuted = BentoTextMuted


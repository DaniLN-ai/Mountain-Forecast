package com.example.model

enum class RegionType {
    HIMALAYAS,
    KARAKORAM,
    ALPS,
    ANDES,
    ROCKIES,
    VOLCANO,
    POLAR,
    PACIFIC,
    BRITISH_ISLES,
    AUSTRALIAN_ALPS
}

data class Mountain(
    val name: String,
    val range: String,
    val country: String,
    val elevationMeters: Int,
    val latitude: Double,
    val longitude: Double,
    val description: String,
    val difficulty: String, // "Extreme Trek", "Technical Climb", "High Altitude Walk", "Alpine Hike", "Touristic"
    val regionType: RegionType,
    val prominenceMeters: Int
) {
    val elevationFeet: Int get() = (elevationMeters * 3.28084).toInt()
}

object MountainRepository {
    val mountains = listOf(
        Mountain(
            name = "Mount Everest",
            range = "Himalayas",
            country = "Nepal / Tibet",
            elevationMeters = 8848,
            latitude = 27.9881,
            longitude = 86.9250,
            description = "The highest point on planet Earth. Subject to extreme high-altitude jet stream winds, sub-zero temperatures, and severe monsoon fronts.",
            difficulty = "Extreme Climb",
            regionType = RegionType.HIMALAYAS,
            prominenceMeters = 8848
        ),
        Mountain(
            name = "K2",
            range = "Karakoram",
            country = "Pakistan / China",
            elevationMeters = 8611,
            latitude = 35.8808,
            longitude = 76.5158,
            description = "The Savage Mountain. Famed for its highly technical routes, unpredictable killer storms, and incredibly steep ice fields.",
            difficulty = "Extreme Climb",
            regionType = RegionType.KARAKORAM,
            prominenceMeters = 4017
        ),
        Mountain(
            name = "Aconcagua",
            range = "Andes",
            country = "Argentina",
            elevationMeters = 6961,
            latitude = -32.6532,
            longitude = -70.0109,
            description = "The highest peak in both the Southern and Western Hemispheres. Known for cold, fast and intense winds called 'el viento blanco'.",
            difficulty = "High Altitude Walk",
            regionType = RegionType.ANDES,
            prominenceMeters = 6961
        ),
        Mountain(
            name = "Denali",
            range = "Alaska Range",
            country = "United States",
            elevationMeters = 6194,
            latitude = 63.0692,
            longitude = -151.0070,
            description = "The coldest mountain on Earth. Famed for its immense topographic relief and extreme Arctic weather near the Arctic Circle.",
            difficulty = "Technical Climb",
            regionType = RegionType.ROCKIES,
            prominenceMeters = 6144
        ),
        Mountain(
            name = "Mount Kilimanjaro",
            range = "Eastern Rift Valley",
            country = "Tanzania",
            elevationMeters = 5895,
            latitude = -3.0674,
            longitude = 37.3556,
            description = "The highest free-standing mountain in the world. Rises through five distinct ecological zones, from tropical rain forest to glacial arctic summit.",
            difficulty = "Alpine Trek",
            regionType = RegionType.VOLCANO,
            prominenceMeters = 5885
        ),
        Mountain(
            name = "Mount Elbrus",
            range = "Caucasus",
            country = "Russia",
            elevationMeters = 5642,
            latitude = 43.3499,
            longitude = 42.4453,
            description = "The highest point in Europe. A dormant double-coned volcano clad in permanent ice fields, susceptible to sudden whiteout blizzards.",
            difficulty = "Glacial Walk",
            regionType = RegionType.VOLCANO,
            prominenceMeters = 4741
        ),
        Mountain(
            name = "Mont Blanc",
            range = "Alps",
            country = "France / Italy",
            elevationMeters = 4808,
            latitude = 45.8327,
            longitude = 6.8652,
            description = "The birthplace of mountaineering. Guarded by complex crevasses and subject to fierce alpine storms.",
            difficulty = "Alpine Climb",
            regionType = RegionType.ALPS,
            prominenceMeters = 4695
        ),
        Mountain(
            name = "Mount Rainier",
            range = "Cascades",
            country = "United States",
            elevationMeters = 4392,
            latitude = 46.8523,
            longitude = -121.7603,
            description = "The most heavily glaciated peak in the contiguous US. Active stratovolcano that intercepts wild moisture systems straight from the Pacific.",
            difficulty = "Glacial Climb",
            regionType = RegionType.VOLCANO,
            prominenceMeters = 4027
        ),
        Mountain(
            name = "Mauna Kea",
            range = "Hawaiian Islands",
            country = "United States",
            elevationMeters = 4207,
            latitude = 19.8206,
            longitude = -155.4681,
            description = "Rises over 10,000 meters from its underwater base. High altitude desert with ultra-dry, stable air preeminent for astronomical observatories.",
            difficulty = "Touristic Drive",
            regionType = RegionType.PACIFIC,
            prominenceMeters = 4207
        ),
        Mountain(
            name = "Mount Fuji",
            range = "Fuji Volcanic Belt",
            country = "Japan",
            elevationMeters = 3776,
            latitude = 35.3606,
            longitude = 138.7274,
            description = "The iconic snow-capped peak of Japan. A perfectly symmetrical volcanic cone where winter climbers face bone-chilling high winds.",
            difficulty = "Steep Trek",
            regionType = RegionType.VOLCANO,
            prominenceMeters = 3776
        ),
        Mountain(
            name = "Aoraki / Mount Cook",
            range = "Southern Alps",
            country = "New Zealand",
            elevationMeters = 3724,
            latitude = -43.5950,
            longitude = 170.1410,
            description = "Heavily glaciated peak flanking the Tasman Sea. Subject to fierce maritime fronts, high precipitation, and treacherous avalanche corridors.",
            difficulty = "Glacial Climb",
            regionType = RegionType.PACIFIC,
            prominenceMeters = 3724
        ),
        Mountain(
            name = "Mount Olympus",
            range = "Olympus Massif",
            country = "Greece",
            elevationMeters = 2917,
            latitude = 40.0856,
            longitude = 22.3585,
            description = "The mythical home of the Greek gods. Characterized by precipitous limestone crags, deeply cut gorges, and volatile Mediterranean thunderstorms.",
            difficulty = "Alpine Hike",
            regionType = RegionType.ALPS,
            prominenceMeters = 2900
        ),
        Mountain(
            name = "Ben Nevis",
            range = "Grampian Mountains",
            country = "United Kingdom",
            elevationMeters = 1345,
            latitude = 56.7969,
            longitude = -5.0036,
            description = "The highest point in the British Isles. Famous for notorious North Face gullies and damp, gale-force Scottish winter conditions.",
            difficulty = "Rough Climb",
            regionType = RegionType.BRITISH_ISLES,
            prominenceMeters = 1345
        ),
        Mountain(
            name = "Mount Kosciuszko",
            range = "Snowy Mountains",
            country = "Australia",
            elevationMeters = 2228,
            latitude = -36.4559,
            longitude = 148.2636,
            description = "Australia's highest mainland peak. Gently rounded granite dome subject to seasonal snow blizzards and high winds.",
            difficulty = "Scenic Hike",
            regionType = RegionType.AUSTRALIAN_ALPS,
            prominenceMeters = 2228
        )
    )
}

package com.jarvis.ironman.core.features

data class JarvisFeature(
    val id: String,
    val name: String,
    val description: String,
    val enabledByDefault: Boolean = false
)

class JarvisFeatureRegistry {

    private val features = linkedMapOf<String, JarvisFeature>()
    private val enabledFeatures = mutableSetOf<String>()

    @Synchronized
    fun register(feature: JarvisFeature) {
        require(feature.id.isNotBlank())
        features[feature.id] = feature

        if (feature.enabledByDefault) {
            enabledFeatures.add(feature.id)
        }
    }

    @Synchronized
    fun enable(featureId: String): Boolean {
        if (!features.containsKey(featureId)) return false
        enabledFeatures.add(featureId)
        return true
    }

    @Synchronized
    fun disable(featureId: String) {
        enabledFeatures.remove(featureId)
    }

    @Synchronized
    fun isEnabled(featureId: String): Boolean {
        return featureId in enabledFeatures
    }

    @Synchronized
    fun getAllFeatures(): List<JarvisFeature> {
        return features.values.toList()
    }

    @Synchronized
    fun getEnabledFeatures(): List<JarvisFeature> {
        return features.values.filter {
            it.id in enabledFeatures
        }
    }
}

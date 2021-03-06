/*==============================================================================
Copyright (c) 2010-2013 QUALCOMM Austria Research Center GmbH.
All Rights Reserved.

@file 
    ImageTargets.cpp

@brief
    Sample for ImageTargets

==============================================================================*/


#include <jni.h>
#include <android/log.h>
#include <stdio.h>
#include <string.h>
#include <assert.h>

#ifdef USE_OPENGL_ES_1_1
#include <GLES/gl.h>
#include <GLES/glext.h>
#else
#include <GLES2/gl2.h>
#include <GLES2/gl2ext.h>
#endif

#include <QCAR/QCAR.h>
#include <QCAR/CameraDevice.h>
#include <QCAR/Renderer.h>
#include <QCAR/VideoBackgroundConfig.h>
#include <QCAR/Trackable.h>
#include <QCAR/TrackableResult.h>
#include <QCAR/Tool.h>
#include <QCAR/Tracker.h>
#include <QCAR/TrackerManager.h>
#include <QCAR/ImageTracker.h>
#include <QCAR/CameraCalibration.h>
#include <QCAR/UpdateCallback.h>
#include <QCAR/DataSet.h>


#include "SampleUtils.h"
#include "Texture.h"
#include "CubeShaders.h"

#ifdef __cplusplus
extern "C"
{
#endif

// OpenGL ES 2.0 specific:
#ifdef USE_OPENGL_ES_2_0
unsigned int shaderProgramID    = 0;
GLint vertexHandle              = 0;
GLint normalHandle              = 0;
GLint textureCoordHandle        = 0;
GLint mvpMatrixHandle           = 0;
GLint texSampler2DHandle        = 0;
#endif

// Screen dimensions:
unsigned int screenWidth        = 0;
unsigned int screenHeight       = 0;

// Indicates whether screen is in portrait (true) or landscape (false) mode
bool isActivityInPortraitMode   = false;

// The projection matrix used for rendering virtual objects:
QCAR::Matrix44F projectionMatrix;

// Constants:
static const float kObjectScale = 3.f;

QCAR::DataSet* dataSetTeaWithTuring = 0;

bool switchDataSetAsap          = false;

// Object to receive update callbacks from QCAR SDK
class StoryScanActivity_UpdateCallback : public QCAR::UpdateCallback {   
    virtual void QCAR_onUpdate(QCAR::State& /*state*/) {
    }
};

StoryScanActivity_UpdateCallback updateCallback;

JNIEXPORT int JNICALL
Java_com_christosc_teawithturing_storyScan_StoryScanActivity_getOpenGlEsVersionNative(JNIEnv *, jobject) {
#ifdef USE_OPENGL_ES_1_1        
    return 1;
#else
    return 2;
#endif
}


JNIEXPORT void JNICALL
Java_com_christosc_teawithturing_storyScan_StoryScanActivity_setActivityPortraitMode(JNIEnv *, jobject, jboolean isPortrait) {
    isActivityInPortraitMode = isPortrait;
}

JNIEXPORT int JNICALL
Java_com_christosc_teawithturing_storyScan_StoryScanActivity_initTracker(JNIEnv *, jobject) {
    LOG("Java_com_christosc_teawithturing_storyScan_StoryScanActivity_initTracker");
    // Initialize the image tracker:
    QCAR::TrackerManager& trackerManager = QCAR::TrackerManager::getInstance();
    QCAR::Tracker* tracker = trackerManager.initTracker(QCAR::Tracker::IMAGE_TRACKER);
    if (tracker == NULL) {
        LOG("Failed to initialize ImageTracker.");
        return 0;
    }
    LOG("Successfully initialized ImageTracker.");
    return 1;
}


JNIEXPORT void JNICALL
Java_com_christosc_teawithturing_storyScan_StoryScanActivity_deinitTracker(JNIEnv *, jobject) {
    LOG("Java_com_christosc_teawithturing_storyScan_StoryScanActivity_deinitTracker");
    // Deinit the image tracker:
    QCAR::TrackerManager& trackerManager = QCAR::TrackerManager::getInstance();
    trackerManager.deinitTracker(QCAR::Tracker::IMAGE_TRACKER);
}

JNIEXPORT int JNICALL
Java_com_christosc_teawithturing_storyScan_StoryScanActivity_loadTrackerData(JNIEnv *, jobject) {
    LOG("Java_com_christosc_teawithturing_storyScan_StoryScanActivity_loadTrackerData");
    
    // Get the image tracker:
    QCAR::TrackerManager& trackerManager = QCAR::TrackerManager::getInstance();
    QCAR::ImageTracker* imageTracker = static_cast<QCAR::ImageTracker*>(trackerManager.getTracker(QCAR::Tracker::IMAGE_TRACKER));
    if (imageTracker == NULL) {
        LOG("Failed to load tracking data set because the ImageTracker has not been initialized.");
        return 0;
    }

    // Create the data sets:
    dataSetTeaWithTuring = imageTracker->createDataSet();
    if (dataSetTeaWithTuring == 0) {
        LOG("Failed to create a new tracking data.");
        return 0;
    }

    // Load the data sets:
    if (!dataSetTeaWithTuring->load("teawithturing.xml", QCAR::DataSet::STORAGE_APPRESOURCE)) {
        LOG("Failed to load data set.");
        return 0;
    }
    // Activate the data set:
    if (!imageTracker->activateDataSet(dataSetTeaWithTuring)) {
        LOG("Failed to activate data set.");
        return 0;
    }

    LOG("Successfully loaded and activated data set.");
    return 1;
}


JNIEXPORT int JNICALL
Java_com_christosc_teawithturing_storyScan_StoryScanActivity_destroyTrackerData(JNIEnv *, jobject) {
    LOG("Java_com_christosc_teawithturing_storyScan_StoryScanActivity_destroyTrackerData");

    // Get the image tracker:
    QCAR::TrackerManager& trackerManager = QCAR::TrackerManager::getInstance();
    QCAR::ImageTracker* imageTracker = static_cast<QCAR::ImageTracker*>(
        trackerManager.getTracker(QCAR::Tracker::IMAGE_TRACKER));
    if (imageTracker == NULL) {
        LOG("Failed to destroy the tracking data set because the ImageTracker has not been initialized.");
        return 0;
    }
    
    if (dataSetTeaWithTuring != 0) {
        if (imageTracker->getActiveDataSet() == dataSetTeaWithTuring &&
            !imageTracker->deactivateDataSet(dataSetTeaWithTuring)) {
            LOG("Failed to destroy the tracking data set TeaWithTuring because the data set could not be deactivated.");
            return 0;
        }

        if (!imageTracker->destroyDataSet(dataSetTeaWithTuring)) {
            LOG("Failed to destroy the tracking data set SetTeaWithTuring.");
            return 0;
        }

        LOG("Successfully destroyed the data set SetTeaWithTuring.");
        dataSetTeaWithTuring = 0;
    }
    return 1;
}

JNIEXPORT void JNICALL
Java_com_christosc_teawithturing_storyScan_StoryScanActivity_onQCARInitializedNative(JNIEnv *, jobject) {
    // Register the update callback where we handle the data set swap:
    QCAR::registerCallback(&updateCallback);
}


JNIEXPORT int JNICALL
Java_com_christosc_teawithturing_storyScan_StoryScanActivityRenderer_renderFrame(JNIEnv *, jobject) {
    //LOG("Java_com_christosc_teawithturing_storyScan_GLRenderer_renderFrame");

    // Clear color and depth buffer 
    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

    // Get the state from QCAR and mark the beginning of a rendering section
    QCAR::State state = QCAR::Renderer::getInstance().begin();
    
    // Explicitly render the Video Background
    QCAR::Renderer::getInstance().drawVideoBackground();
       
#ifdef USE_OPENGL_ES_1_1
    // Set GL11 flags:
    glEnableClientState(GL_VERTEX_ARRAY);
    glEnableClientState(GL_NORMAL_ARRAY);
    glEnableClientState(GL_TEXTURE_COORD_ARRAY);

    glEnable(GL_TEXTURE_2D);
    glDisable(GL_LIGHTING);
        
#endif

    glEnable(GL_DEPTH_TEST);

    // We must detect if background reflection is active and adjust the culling direction. 
    // If the reflection is active, this means the post matrix has been reflected as well,
    // therefore standard counter clockwise face culling will result in "inside out" models. 
    glEnable(GL_CULL_FACE);
    glCullFace(GL_BACK);
    if(QCAR::Renderer::getInstance().getVideoBackgroundConfig().mReflection == QCAR::VIDEO_BACKGROUND_REFLECTION_ON)
        glFrontFace(GL_CW);  //Front camera
    else
        glFrontFace(GL_CCW);   //Back camera


    // Did we find any trackables this frame?
    for(int tIdx = 0; tIdx < state.getNumTrackableResults(); tIdx++) {
        // Get the trackable:
        const QCAR::TrackableResult* result = state.getTrackableResult(tIdx);
        const QCAR::Trackable& trackable = result->getTrackable();
        QCAR::Matrix44F modelViewMatrix = QCAR::Tool::convertPose2GLMatrix(result->getPose());        

        // Choose the texture based on the target name:
        int textureIndex;
        if (strcmp(trackable.getName(), "marker_tea") == 0) {
            textureIndex = 0;
        }
        else if (strcmp(trackable.getName(), "marker_trees") == 0) {
            textureIndex = 1;
        }
		else if (strcmp(trackable.getName(), "marker_desk") == 0) {
            textureIndex = 2;
        }
        else {
            textureIndex = -1;
        }
//        SampleUtils::checkGlError("ImageTargets renderFrame");
		return textureIndex;
    }

    glDisable(GL_DEPTH_TEST);

#ifdef USE_OPENGL_ES_1_1        
    glDisable(GL_TEXTURE_2D);
    glDisableClientState(GL_VERTEX_ARRAY);
    glDisableClientState(GL_NORMAL_ARRAY);
    glDisableClientState(GL_TEXTURE_COORD_ARRAY);
#endif

    QCAR::Renderer::getInstance().end();
	
	return -1;
}

void configureVideoBackground() {
    // Get the default video mode:
    QCAR::CameraDevice& cameraDevice = QCAR::CameraDevice::getInstance();
    QCAR::VideoMode videoMode = cameraDevice.getVideoMode(QCAR::CameraDevice::MODE_DEFAULT);


    // Configure the video background
    QCAR::VideoBackgroundConfig config;
    config.mEnabled = true;
    config.mSynchronous = true;
    config.mPosition.data[0] = 0.0f;
    config.mPosition.data[1] = 0.0f;
    
    if (isActivityInPortraitMode) {
        //LOG("configureVideoBackground PORTRAIT");
        config.mSize.data[0] = videoMode.mHeight * (screenHeight / (float)videoMode.mWidth);
        config.mSize.data[1] = screenHeight;

        if(config.mSize.data[0] < screenWidth)
        {
            LOG("Correcting rendering background size to handle missmatch between screen and video aspect ratios.");
            config.mSize.data[0] = screenWidth;
            config.mSize.data[1] = screenWidth * (videoMode.mWidth / (float)videoMode.mHeight);
        }
    }
    else {
        //LOG("configureVideoBackground LANDSCAPE");
        config.mSize.data[0] = screenWidth;
        config.mSize.data[1] = videoMode.mHeight * (screenWidth / (float)videoMode.mWidth);

        if(config.mSize.data[1] < screenHeight)
        {
            LOG("Correcting rendering background size to handle missmatch between screen and video aspect ratios.");
            config.mSize.data[0] = screenHeight
                                * (videoMode.mWidth / (float)videoMode.mHeight);
            config.mSize.data[1] = screenHeight;
        }
    }

    LOG("Configure Video Background : Video (%d,%d), Screen (%d,%d), mSize (%d,%d)", videoMode.mWidth, videoMode.mHeight, screenWidth, screenHeight, config.mSize.data[0], config.mSize.data[1]);

    // Set the config:
    QCAR::Renderer::getInstance().setVideoBackgroundConfig(config);
}


JNIEXPORT void JNICALL
Java_com_christosc_teawithturing_storyScan_StoryScanActivity_initApplicationNative(
                            JNIEnv* env, jobject obj, jint width, jint height) {
    LOG("Java_com_christosc_teawithturing_storyScan_StoryScanActivity_initApplicationNative");
    
    // Store screen dimensions
    screenWidth = width;
    screenHeight = height;
        
    // Handle to the activity class:
    jclass activityClass = env->GetObjectClass(obj);

    LOG("Java_com_christosc_teawithturing_storyScan_StoryScanActivity_initApplicationNative finished");
}


JNIEXPORT void JNICALL
Java_com_christosc_teawithturing_storyScan_StoryScanActivity_deinitApplicationNative(JNIEnv* env, jobject obj) {
    LOG("Java_com_christosc_teawithturing_storyScan_StoryScanActivity_deinitApplicationNative");
}


JNIEXPORT void JNICALL
Java_com_christosc_teawithturing_storyScan_StoryScanActivity_startCamera(JNIEnv *, jobject) {
    LOG("Java_com_christosc_teawithturing_storyScan_StoryScanActivity_startCamera");
    
    // Select the camera to open, set this to QCAR::CameraDevice::CAMERA_FRONT 
    // to activate the front camera instead.
    QCAR::CameraDevice::CAMERA camera = QCAR::CameraDevice::CAMERA_DEFAULT;

    // Initialize the camera:
    if (!QCAR::CameraDevice::getInstance().init(camera))
        return;

    // Configure the video background
    configureVideoBackground();

    // Select the default mode:
    if (!QCAR::CameraDevice::getInstance().selectVideoMode(QCAR::CameraDevice::MODE_DEFAULT))
        return;

    // Start the camera:
    if (!QCAR::CameraDevice::getInstance().start())
        return;

    // Uncomment to enable flash
    //if(QCAR::CameraDevice::getInstance().setFlashTorchMode(true))
    //    LOG("IMAGE TARGETS : enabled torch");

    // Uncomment to enable infinity focus mode, or any other supported focus mode
    // See CameraDevice.h for supported focus modes
    //if(QCAR::CameraDevice::getInstance().setFocusMode(QCAR::CameraDevice::FOCUS_MODE_INFINITY))
    //    LOG("IMAGE TARGETS : enabled infinity focus");

    // Start the tracker:
    QCAR::TrackerManager& trackerManager = QCAR::TrackerManager::getInstance();
    QCAR::Tracker* imageTracker = trackerManager.getTracker(QCAR::Tracker::IMAGE_TRACKER);
    if(imageTracker != 0)
        imageTracker->start();
}


JNIEXPORT void JNICALL
Java_com_christosc_teawithturing_storyScan_StoryScanActivity_stopCamera(JNIEnv *, jobject) {
    LOG("Java_com_christosc_teawithturing_storyScan_StoryScanActivity_stopCamera");

    // Stop the tracker:
    QCAR::TrackerManager& trackerManager = QCAR::TrackerManager::getInstance();
    QCAR::Tracker* imageTracker = trackerManager.getTracker(QCAR::Tracker::IMAGE_TRACKER);
    if(imageTracker != 0)
        imageTracker->stop();
    
    QCAR::CameraDevice::getInstance().stop();
    QCAR::CameraDevice::getInstance().deinit();
}

JNIEXPORT void JNICALL
Java_com_christosc_teawithturing_storyScan_StoryScanActivity_setProjectionMatrix(JNIEnv *, jobject) {
    LOG("Java_com_christosc_teawithturing_storyScan_StoryScanActivity_setProjectionMatrix");

    // Cache the projection matrix:
    const QCAR::CameraCalibration& cameraCalibration = QCAR::CameraDevice::getInstance().getCameraCalibration();
    projectionMatrix = QCAR::Tool::getProjectionGL(cameraCalibration, 2.0f, 2500.0f);
}

// ----------------------------------------------------------------------------
// Activates Camera Flash
// ----------------------------------------------------------------------------
JNIEXPORT jboolean JNICALL
Java_com_christosc_teawithturing_storyScan_StoryScanActivity_activateFlash(JNIEnv*, jobject, jboolean flash) {
    return QCAR::CameraDevice::getInstance().setFlashTorchMode((flash==JNI_TRUE)) ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL
Java_com_christosc_teawithturing_storyScan_StoryScanActivity_autofocus(JNIEnv*, jobject)  {
    return QCAR::CameraDevice::getInstance().setFocusMode(QCAR::CameraDevice::FOCUS_MODE_TRIGGERAUTO) ? JNI_TRUE : JNI_FALSE;
}


JNIEXPORT jboolean JNICALL
Java_com_christosc_teawithturing_storyScan_StoryScanActivity_setFocusMode(JNIEnv*, jobject, jint mode)  {
    int qcarFocusMode;

    switch ((int)mode) {
        case 0:
            qcarFocusMode = QCAR::CameraDevice::FOCUS_MODE_NORMAL;
            break;
        
        case 1:
            qcarFocusMode = QCAR::CameraDevice::FOCUS_MODE_CONTINUOUSAUTO;
            break;
            
        case 2:
            qcarFocusMode = QCAR::CameraDevice::FOCUS_MODE_INFINITY;
            break;
            
        case 3:
            qcarFocusMode = QCAR::CameraDevice::FOCUS_MODE_MACRO;
            break;
    
        default:
            return JNI_FALSE;
    }
    
    return QCAR::CameraDevice::getInstance().setFocusMode(qcarFocusMode) ? JNI_TRUE : JNI_FALSE;
}


JNIEXPORT void JNICALL
Java_com_christosc_teawithturing_storyScan_StoryScanActivityRenderer_initRendering(JNIEnv* env, jobject obj) {
    LOG("Java_com_christosc_teawithturing_storyScan_StoryScanActivity_StoryScanActivityRenderer_initRendering");

    // Define clear color
    glClearColor(0.0f, 0.0f, 0.0f, QCAR::requiresAlpha() ? 0.0f : 1.0f);
    
//#ifndef USE_OPENGL_ES_1_1
//    shaderProgramID     = SampleUtils::createProgramFromBuffer(cubeMeshVertexShader, cubeFragmentShader);
//    vertexHandle        = glGetAttribLocation(shaderProgramID, "vertexPosition");
//    normalHandle        = glGetAttribLocation(shaderProgramID, "vertexNormal");
//    textureCoordHandle  = glGetAttribLocation(shaderProgramID, "vertexTexCoord");
//    mvpMatrixHandle     = glGetUniformLocation(shaderProgramID, "modelViewProjectionMatrix");
//    texSampler2DHandle  = glGetUniformLocation(shaderProgramID, "texSampler2D");                                          
//#endif
}

JNIEXPORT void JNICALL
Java_com_christosc_teawithturing_storyScan_StoryScanActivityRenderer_updateRendering(
                        JNIEnv* env, jobject obj, jint width, jint height){
    LOG("Java_com_christosc_teawithturing_storyScan_StoryScanActivityRenderer_updateRendering");

    // Update screen dimensions
    screenWidth = width;
    screenHeight = height;

    // Reconfigure the video background
    configureVideoBackground();
}

#ifdef __cplusplus
}
#endif

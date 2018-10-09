package fi.metropolia.alkompassi.ar

import android.animation.ObjectAnimator
import android.net.Uri
import android.os.Bundle
import com.google.ar.sceneform.ux.ArFragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import androidx.fragment.app.Fragment
import com.google.ar.core.*
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.QuaternionEvaluator
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.ux.BaseArFragment
import com.google.ar.sceneform.ux.TransformableNode
import fi.metropolia.alkompassi.R
import fi.metropolia.alkompassi.data.TempData
import fi.metropolia.alkompassi.ui.maps.MapsViewModel

class ArFragment : Fragment(){

    private lateinit var ar: View

    private lateinit var fragment : ArFragment

    private lateinit var modelUri : Uri
    private lateinit var testRenderable : ModelRenderable

    lateinit var anchorNode : AnchorNode
    lateinit var viewNode : TransformableNode

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        ar = inflater.inflate(R.layout.ar_fragment, container, false)

        fragment = childFragmentManager.findFragmentById(R.id.sceneform_fragment) as ArFragment

        modelUri = Uri.parse("arrow2.sfb")

        val renderableFuture = ModelRenderable.builder().setSource(this.context, modelUri).build()
        renderableFuture.thenAccept { it -> testRenderable = it }

        return ar
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        fragment.setOnTapArPlaneListener(
                object : BaseArFragment.OnTapArPlaneListener {
                    override fun onTapPlane(hitResult: HitResult?, plane: Plane?, motionEvent: MotionEvent?) {

                        if (testRenderable == null) {
                            return@onTapPlane
                        }

                                val anchor = hitResult!!.createAnchor()
                                anchorNode = AnchorNode(anchor)
                                anchorNode.setParent(fragment.arSceneView.scene)
                                viewNode = TransformableNode(fragment.transformationSystem)

                                viewNode.setParent(anchorNode)
                                viewNode.renderable = testRenderable
                                viewNode.select()
                                MapsViewModel().refreshDegrees(TempData.myLat ,TempData.myLng)
                        val rotationDegrees = TempData.rotationDegrees - 180
                        val alkoDegrees = rotationDegrees - TempData.alkoDegrees
                                rotateNode(rotationDegrees.toDouble(), alkoDegrees)
                    }
                }
        )
    }

    private fun rotateNode(rotationDirection: Double, alkoDirection: Double) {

        var orbitAnimator = ObjectAnimator()
        orbitAnimator.target = viewNode

        val orientation1 = Quaternion.axisAngle(Vector3(0.0f, 1.0f, 0.0f), rotationDirection.toFloat())
        val orientation2 = Quaternion.axisAngle(Vector3(0.0f, 1.0f, 0.0f), alkoDirection.toFloat())

        orbitAnimator.duration = 500

        orbitAnimator.setObjectValues(orientation1, orientation2)

        orbitAnimator.propertyName = "localRotation"
        orbitAnimator.setEvaluator(QuaternionEvaluator())
        orbitAnimator.interpolator = LinearInterpolator()
        orbitAnimator.setAutoCancel(true)
        orbitAnimator.start()

    }
}
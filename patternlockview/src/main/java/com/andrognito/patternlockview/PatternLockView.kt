package com.andrognito.patternlockview

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.os.Build
import android.os.Debug
import android.os.Parcel
import android.os.Parcelable
import android.os.SystemClock
import android.util.AttributeSet
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityManager
import android.view.animation.AnimationUtils
import android.view.animation.Interpolator
import com.andrognito.patternlockview.listener.PatternLockViewListener
import com.andrognito.patternlockview.utils.PatternLockUtils
import com.andrognito.patternlockview.utils.ResourceUtils

/**
 * Displays a powerful, customizable and Material Design complaint pattern lock in the screen which
 * can be used to lock any Activity or Fragment from the user
 */
class PatternLockView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
    View(context, attrs) {
    /**
     * Represents the aspect ratio for the View
     */
    enum class AspectRatio(val value: Int){
        // Width and height will be same. Minimum of width and height
        ASPECT_RATIO_SQUARE(0),

        // Width will be fixed. The height will be the minimum of width and height
        ASPECT_RATIO_WIDTH_BIAS(1),

        // Height will be fixed. The width will be the minimum of width and height
        ASPECT_RATIO_HEIGHT_BIAS(2)


    }

    /**
     * Represents the different modes in which this view can be represented
     */
    enum class PatternViewMode(val value: Int){
        /**
         * This state represents a correctly drawn pattern by the user. The color of the path and
         * the dots both would be changed to this color.
         *
         *
         * (NOTE - Consider showing this state in a friendly color)
         */
        CORRECT(0),

        /**
         * Automatically draw the pattern for demo or tutorial purposes.
         */
        AUTO_DRAW(1),

        /**
         * This state represents a wrongly drawn pattern by the user. The color of the path and
         * the dots both would be changed to this color.
         *
         *
         * (NOTE - Consider showing this state in an attention-seeking color)
         */
        WRONG(2)
    }

    private var mDotStates: Array<Array<DotState?>>
    var patternSize: Int
        private set
    private var mDrawingProfilingStarted = false
    private var mAnimatingPeriodStart: Long = 0
    private val mHitFactor = 0.6f
    private var mAspectRatioEnabled = false
    private var mAspectRatio = AspectRatio.ASPECT_RATIO_SQUARE.value
    var normalStateColor = 0
    var wrongStateColor = 0
    var correctStateColor = 0
    private var mPathWidth = 0
    private var mDotNormalSize = 0
    var dotSelectedSize = 0
    private var mDotAnimationDuration = 0
    var pathEndAnimationDuration = 0
    private var mDotPaint: Paint? = null
    private var mPathPaint: Paint? = null
    private val mPatternListeners: MutableList<PatternLockViewListener>

    // The pattern represented as a list of connected {@link Dot}
    private var mPattern: ArrayList<Dot>

    /**
     * Lookup table for the dots of the pattern we are currently drawing.
     * This will be the dots of the complete pattern unless we are animating,
     * in which case we use this to hold the dots we are drawing for the in
     * progress animation.
     */
    private var mPatternDrawLookup: Array<BooleanArray>
    private var mInProgressX = -1f
    private var mInProgressY = -1f

    var patternViewMode = PatternViewMode.CORRECT
        private set

    /**
     * Enabled/disables any user input of the view. This can be useful to lock the view temporarily
     * while showing any message to the user so that the user cannot get the view in
     * an unwanted state
     */
    var isInputEnabled = true

    /**
     * Set whether the View is in stealth mode. If `true`, there will be
     * no visible feedback (path drawing, dot animating, etc) as the user enters the pattern
     */
    var isInStealthMode = false
    var isTactileFeedbackEnabled = true
    private var mPatternInProgress = false
    private var mViewWidth = 0f
    private var mViewHeight = 0f
    private val mCurrentPath = Path()
    private val mInvalidate = Rect()
    private val mTempInvalidateRect = Rect()
    private var mFastOutSlowInInterpolator: Interpolator? = null
    private var mLinearOutSlowInInterpolator: Interpolator? = null

    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.PatternLockView)
        try {
            sDotCount = typedArray.getInt(
                R.styleable.PatternLockView_dotCount,
                DEFAULT_PATTERN_DOT_COUNT
            )
            mAspectRatioEnabled = typedArray.getBoolean(
                R.styleable.PatternLockView_aspectRatioEnabled,
                false
            )
            mAspectRatio = typedArray.getInt(
                R.styleable.PatternLockView_aspectRatio,
                AspectRatio.ASPECT_RATIO_SQUARE.value
            )

            mPathWidth = typedArray.getDimension(
                R.styleable.PatternLockView_pathWidth,
                ResourceUtils.getDimensionInPx(getContext(), R.dimen.pattern_lock_path_width)
            ).toInt()
            normalStateColor = typedArray.getColor(
                R.styleable.PatternLockView_normalStateColor,
                ResourceUtils.getColor(getContext(), R.color.white)
            )
            correctStateColor = typedArray.getColor(
                R.styleable.PatternLockView_correctStateColor,
                ResourceUtils.getColor(getContext(), R.color.white)
            )
            wrongStateColor = typedArray.getColor(
                R.styleable.PatternLockView_wrongStateColor,
                ResourceUtils.getColor(getContext(), R.color.pomegranate)
            )
            mDotNormalSize = typedArray.getDimension(
                R.styleable.PatternLockView_dotNormalSize,
                ResourceUtils.getDimensionInPx(getContext(), R.dimen.pattern_lock_dot_size)
            ).toInt()
            dotSelectedSize = typedArray.getDimension(
                R.styleable.PatternLockView_dotSelectedSize,
                ResourceUtils.getDimensionInPx(getContext(), R.dimen.pattern_lock_dot_selected_size)
            ).toInt()
            mDotAnimationDuration = typedArray.getInt(
                R.styleable.PatternLockView_dotAnimationDuration,
                DEFAULT_DOT_ANIMATION_DURATION
            )
            pathEndAnimationDuration = typedArray.getInt(
                R.styleable.PatternLockView_pathEndAnimationDuration,
                DEFAULT_PATH_END_ANIMATION_DURATION
            )
        } finally {
            typedArray.recycle()
        }

        // The pattern will always be symmetrical
        patternSize = sDotCount * sDotCount
        mPattern = ArrayList(patternSize)
        mPatternDrawLookup = Array(sDotCount) { BooleanArray(sDotCount) }
        mDotStates = Array(sDotCount) { arrayOfNulls(sDotCount) }
        for (i in 0 until sDotCount) {
            for (j in 0 until sDotCount) {
                mDotStates[i][j] = DotState()
                mDotStates[i][j]!!.mSize = mDotNormalSize.toFloat()
            }
        }
        mPatternListeners = ArrayList()
        initView()
    }

    private fun initView() {
        isClickable = true
        mPathPaint = Paint()
        mPathPaint!!.isAntiAlias = true
        mPathPaint!!.isDither = true
        mPathPaint!!.color = normalStateColor
        mPathPaint!!.style = Paint.Style.STROKE
        mPathPaint!!.strokeJoin = Paint.Join.ROUND
        mPathPaint!!.strokeCap = Paint.Cap.ROUND
        mPathPaint!!.strokeWidth = mPathWidth.toFloat()
        mDotPaint = Paint()
        mDotPaint!!.isAntiAlias = true
        mDotPaint!!.isDither = true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
            && !isInEditMode
        ) {
            mFastOutSlowInInterpolator = AnimationUtils.loadInterpolator(
                context, android.R.interpolator.fast_out_slow_in
            )
            mLinearOutSlowInInterpolator = AnimationUtils.loadInterpolator(
                context, android.R.interpolator.linear_out_slow_in
            )
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        if (!mAspectRatioEnabled) {
            return
        }
        val oldWidth = resolveMeasured(widthMeasureSpec, suggestedMinimumWidth)
        val oldHeight = resolveMeasured(heightMeasureSpec, suggestedMinimumHeight)
        val newWidth: Int
        val newHeight: Int
        when (mAspectRatio) {
            AspectRatio.ASPECT_RATIO_SQUARE.value -> {
                newHeight = Math.min(oldWidth, oldHeight)
                newWidth = newHeight
            }

            AspectRatio.ASPECT_RATIO_WIDTH_BIAS.value -> {
                newWidth = oldWidth
                newHeight = Math.min(oldWidth, oldHeight)
            }

            AspectRatio.ASPECT_RATIO_HEIGHT_BIAS.value -> {
                newWidth = Math.min(oldWidth, oldHeight)
                newHeight = oldHeight
            }

            else -> throw IllegalStateException("Unknown aspect ratio")
        }
        setMeasuredDimension(newWidth, newHeight)
    }

    override fun onDraw(canvas: Canvas) {
        val pattern = mPattern
        val patternSize = pattern.size
        val drawLookupTable = mPatternDrawLookup
        if (patternViewMode == PatternViewMode.AUTO_DRAW) {
            val oneCycle = (patternSize + 1) * MILLIS_PER_CIRCLE_ANIMATING
            val spotInCycle =
                (SystemClock.elapsedRealtime() - mAnimatingPeriodStart).toInt() % oneCycle
            val numCircles = spotInCycle / MILLIS_PER_CIRCLE_ANIMATING
            clearPatternDrawLookup()
            for (i in 0 until numCircles) {
                val dot = pattern[i]
                drawLookupTable[dot!!.row][dot.column] = true
            }
            val needToUpdateInProgressPoint = (numCircles > 0
                    && numCircles < patternSize)
            if (needToUpdateInProgressPoint) {
                val percentageOfNextCircle = ((spotInCycle % MILLIS_PER_CIRCLE_ANIMATING).toFloat()
                        / MILLIS_PER_CIRCLE_ANIMATING)
                val currentDot = pattern[numCircles - 1]
                val centerX = getCenterXForColumn(currentDot!!.column)
                val centerY = getCenterYForRow(currentDot.row)
                val nextDot = pattern[numCircles]
                val dx = (percentageOfNextCircle
                        * (getCenterXForColumn(nextDot!!.column) - centerX))
                val dy = (percentageOfNextCircle
                        * (getCenterYForRow(nextDot.row) - centerY))
                mInProgressX = centerX + dx
                mInProgressY = centerY + dy
            }
            invalidate()
        }
        val currentPath = mCurrentPath
        currentPath.rewind()

        // Draw the dots
        for (i in 0 until sDotCount) {
            val centerY = getCenterYForRow(i)
            for (j in 0 until sDotCount) {
                val dotState = mDotStates[i][j]
                val centerX = getCenterXForColumn(j)
                val size = dotState!!.mSize * dotState.mScale
                val translationY = dotState.mTranslateY
                drawCircle(
                    canvas, centerX.toInt().toFloat(), centerY.toInt() + translationY,
                    size, drawLookupTable[i][j], dotState.mAlpha
                )
            }
        }

        // Draw the path of the pattern (unless we are in stealth mode)
        val drawPath = !isInStealthMode
        if (drawPath) {
            mPathPaint!!.color = getCurrentColor(true)
            var anyCircles = false
            var lastX = 0f
            var lastY = 0f
            for (i in 0 until patternSize) {
                val dot = pattern[i]

                // Only draw the part of the pattern stored in
                // the lookup table (this is only different in case
                // of animation)
                if (!drawLookupTable[dot!!.row][dot.column]) {
                    break
                }
                anyCircles = true
                val centerX = getCenterXForColumn(dot.column)
                val centerY = getCenterYForRow(dot.row)
                if (i != 0) {
                    val state = mDotStates[dot.row][dot.column]
                    currentPath.rewind()
                    currentPath.moveTo(lastX, lastY)
                    if (state!!.mLineEndX != Float.MIN_VALUE
                        && state.mLineEndY != Float.MIN_VALUE
                    ) {
                        currentPath.lineTo(state.mLineEndX, state.mLineEndY)
                    } else {
                        currentPath.lineTo(centerX, centerY)
                    }
                    canvas.drawPath(currentPath, mPathPaint!!)
                }
                lastX = centerX
                lastY = centerY
            }

            // Draw last in progress section
            if ((mPatternInProgress || patternViewMode == PatternViewMode.AUTO_DRAW)
                && anyCircles
            ) {
                currentPath.rewind()
                currentPath.moveTo(lastX, lastY)
                currentPath.lineTo(mInProgressX, mInProgressY)
                mPathPaint!!.alpha = (calculateLastSegmentAlpha(
                    mInProgressX, mInProgressY, lastX, lastY
                ) * 255f).toInt()
                canvas.drawPath(currentPath, mPathPaint!!)
            }
        }
    }

    override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        val adjustedWidth = width - paddingLeft - paddingRight
        mViewWidth = adjustedWidth / sDotCount.toFloat()
        val adjustedHeight = height - paddingTop - paddingBottom
        mViewHeight = adjustedHeight / sDotCount.toFloat()
    }

    override fun onSaveInstanceState(): Parcelable? {
        val superState = super.onSaveInstanceState()
        return SavedState(
            superState,
            PatternLockUtils.patternToString(this, mPattern),
            patternViewMode.value, isInputEnabled, isInStealthMode,
            isTactileFeedbackEnabled
        )
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        val savedState = state as SavedState
        super.onRestoreInstanceState(savedState.superState)
        setPattern(
            PatternViewMode.CORRECT,
            PatternLockUtils.stringToPattern(this, savedState.serializedPattern!!)
        )
        patternViewMode = PatternViewMode.valueOf(savedState.displayMode.toString())
        isInputEnabled = savedState.isInputEnabled
        isInStealthMode = savedState.isInStealthMode
        isTactileFeedbackEnabled = savedState.isTactileFeedbackEnabled
    }

    override fun onHoverEvent(event: MotionEvent): Boolean {
        if ((context.getSystemService(
                Context.ACCESSIBILITY_SERVICE
            ) as AccessibilityManager).isTouchExplorationEnabled
        ) {
            val action = event.action
            when (action) {
                MotionEvent.ACTION_HOVER_ENTER -> event.action = MotionEvent.ACTION_DOWN
                MotionEvent.ACTION_HOVER_MOVE -> event.action = MotionEvent.ACTION_MOVE
                MotionEvent.ACTION_HOVER_EXIT -> event.action = MotionEvent.ACTION_UP
            }
            onTouchEvent(event)
            event.action = action
        }
        return super.onHoverEvent(event)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isInputEnabled || !isEnabled) {
            return false
        }
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                handleActionDown(event)
                return true
            }

            MotionEvent.ACTION_UP -> {
                handleActionUp(event)
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                handleActionMove(event)
                return true
            }

            MotionEvent.ACTION_CANCEL -> {
                mPatternInProgress = false
                resetPattern()
                notifyPatternCleared()
                if (PROFILE_DRAWING) {
                    if (mDrawingProfilingStarted) {
                        Debug.stopMethodTracing()
                        mDrawingProfilingStarted = false
                    }
                }
                return true
            }
        }
        return false
    }

    val pattern: List<Dot>
        /**
         * Returns the list of dots in the current selected pattern. This list is independent of the
         * internal pattern dot list
         */
        get() = mPattern.clone() as List<Dot>
    var dotCount: Int
        get() = sDotCount
        set(dotCount) {
            sDotCount = dotCount
            patternSize = sDotCount * sDotCount
            mPattern = ArrayList(patternSize)
            mPatternDrawLookup = Array(sDotCount) { BooleanArray(sDotCount) }
            mDotStates = Array(sDotCount) { arrayOfNulls(sDotCount) }
            for (i in 0 until sDotCount) {
                for (j in 0 until sDotCount) {
                    mDotStates[i][j] = DotState()
                    mDotStates[i][j]!!.mSize = mDotNormalSize.toFloat()
                }
            }
            requestLayout()
            invalidate()
        }
    var isAspectRatioEnabled: Boolean
        get() = mAspectRatioEnabled
        set(aspectRatioEnabled) {
            mAspectRatioEnabled = aspectRatioEnabled
            requestLayout()
        }

    var aspectRatio: Int
        get() = mAspectRatio
        set(aspectRatio) {
            mAspectRatio = aspectRatio
            requestLayout()
        }
    var pathWidth: Int
        get() = mPathWidth
        set(pathWidth) {
            mPathWidth = pathWidth
            initView()
            invalidate()
        }
    var dotNormalSize: Int
        get() = mDotNormalSize
        set(dotNormalSize) {
            mDotNormalSize = dotNormalSize
            for (i in 0 until sDotCount) {
                for (j in 0 until sDotCount) {
                    mDotStates[i][j] = DotState()
                    mDotStates[i][j]!!.mSize = mDotNormalSize.toFloat()
                }
            }
            invalidate()
        }
    var dotAnimationDuration: Int
        get() = mDotAnimationDuration
        set(dotAnimationDuration) {
            mDotAnimationDuration = dotAnimationDuration
            invalidate()
        }

    /**
     * Set the pattern explicitly rather than waiting for the user to input a
     * pattern. You can use this for help or demo purposes
     *
     * @param patternViewMode The mode in which the pattern should be displayed
     * @param pattern         The pattern
     */
    fun setPattern(patternViewMode: PatternViewMode, pattern: List<Dot>) {
        mPattern.clear()
        mPattern.addAll(pattern)
        clearPatternDrawLookup()
        for (dot in pattern) {
            mPatternDrawLookup[dot!!.row][dot.column] = true
        }
        setViewMode(patternViewMode)
    }

    /**
     * Set the display mode of the current pattern. This can be useful, for
     * instance, after detecting a pattern to tell this view whether change the
     * in progress result to correct or wrong.
     */
    fun setViewMode(patternViewMode: PatternViewMode) {
        this.patternViewMode = patternViewMode
        if (patternViewMode == PatternViewMode.AUTO_DRAW) {
            check(mPattern.size != 0) {
                ("you must have a pattern to "
                        + "animate if you want to set the display mode to animate")
            }
            mAnimatingPeriodStart = SystemClock.elapsedRealtime()
            val first = mPattern[0]
            mInProgressX = getCenterXForColumn(first!!.column)
            mInProgressY = getCenterYForRow(first.row)
            clearPatternDrawLookup()
        }
        invalidate()
    }

    fun setEnableHapticFeedback(enableHapticFeedback: Boolean) {
        isTactileFeedbackEnabled = enableHapticFeedback
    }

    fun addPatternLockListener(patternListener: PatternLockViewListener) {
        mPatternListeners.add(patternListener)
    }

    fun removePatternLockListener(patternListener: PatternLockViewListener) {
        mPatternListeners.remove(patternListener)
    }

    fun clearPattern() {
        resetPattern()
    }

    private fun resolveMeasured(measureSpec: Int, desired: Int): Int {
        val result: Int
        val specSize = MeasureSpec.getSize(measureSpec)
        result = when (MeasureSpec.getMode(measureSpec)) {
            MeasureSpec.UNSPECIFIED -> desired
            MeasureSpec.AT_MOST -> Math.max(specSize, desired)
            MeasureSpec.EXACTLY -> specSize
            else -> specSize
        }
        return result
    }

    private fun notifyPatternProgress() {
        sendAccessEvent(R.string.message_pattern_dot_added)
        notifyListenersProgress(mPattern)
    }

    private fun notifyPatternStarted() {
        sendAccessEvent(R.string.message_pattern_started)
        notifyListenersStarted()
    }

    private fun notifyPatternDetected() {
        sendAccessEvent(R.string.message_pattern_detected)
        notifyListenersComplete(mPattern)
    }

    private fun notifyPatternCleared() {
        sendAccessEvent(R.string.message_pattern_cleared)
        notifyListenersCleared()
    }

    private fun resetPattern() {
        mPattern.clear()
        clearPatternDrawLookup()
        patternViewMode = PatternViewMode.CORRECT
        invalidate()
    }

    private fun notifyListenersStarted() {
        for (patternListener in mPatternListeners) {
            patternListener?.onStarted()
        }
    }

    private fun notifyListenersProgress(pattern: List<Dot?>) {
        for (patternListener in mPatternListeners) {
            patternListener.onProgress(pattern)
        }
    }

    private fun notifyListenersComplete(pattern: List<Dot>?) {
        for (patternListener in mPatternListeners) {
            patternListener.onComplete(pattern)
        }
    }

    private fun notifyListenersCleared() {
        for (patternListener in mPatternListeners) {
            patternListener.onCleared()
        }
    }

    private fun clearPatternDrawLookup() {
        for (i in 0 until sDotCount) {
            for (j in 0 until sDotCount) {
                mPatternDrawLookup[i][j] = false
            }
        }
    }

    /**
     * Determines whether the point x, y will add a new point to the current
     * pattern (in addition to finding the dot, also makes heuristic choices
     * such as filling in gaps based on current pattern).
     *
     * @param x The x coordinate
     * @param y The y coordinate
     */
    private fun detectAndAddHit(x: Float, y: Float): Dot? {
        val dot = checkForNewHit(x, y)
        if (dot != null) {
            // Check for gaps in existing pattern
            var fillInGapDot: Dot? = null
            val pattern = mPattern
            if (!pattern.isEmpty()) {
                val lastDot = pattern[pattern.size - 1]
                val dRow = dot.row - lastDot!!.row
                val dColumn = dot.column - lastDot.column
                var fillInRow = lastDot.row
                var fillInColumn = lastDot.column
                if (Math.abs(dRow) == 2 && Math.abs(dColumn) != 1) {
                    fillInRow = lastDot.row + if (dRow > 0) 1 else -1
                }
                if (Math.abs(dColumn) == 2 && Math.abs(dRow) != 1) {
                    fillInColumn = lastDot.column + if (dColumn > 0) 1 else -1
                }
                fillInGapDot = Dot.of(fillInRow, fillInColumn)
            }
            if (fillInGapDot != null
                && !mPatternDrawLookup[fillInGapDot.row][fillInGapDot.column]
            ) {
                addCellToPattern(fillInGapDot)
            }
            addCellToPattern(dot)
            if (isTactileFeedbackEnabled) {
                performHapticFeedback(
                    HapticFeedbackConstants.VIRTUAL_KEY,
                    HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING
                            or HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING
                )
            }
            return dot
        }
        return null
    }

    private fun addCellToPattern(newDot: Dot) {
        mPatternDrawLookup[newDot.row][newDot.column] = true
        mPattern.add(newDot)
        if (!isInStealthMode) {
            startDotSelectedAnimation(newDot)
        }
        notifyPatternProgress()
    }

    private fun startDotSelectedAnimation(dot: Dot) {
        val dotState = mDotStates[dot.row][dot.column]
        startSizeAnimation(
            mDotNormalSize.toFloat(), dotSelectedSize.toFloat(), mDotAnimationDuration.toLong(),
            mLinearOutSlowInInterpolator, dotState
        ) {
            startSizeAnimation(
                dotSelectedSize.toFloat(), mDotNormalSize.toFloat(), mDotAnimationDuration.toLong(),
                mFastOutSlowInInterpolator, dotState, null
            )
        }
        startLineEndAnimation(
            dotState, mInProgressX, mInProgressY,
            getCenterXForColumn(dot.column), getCenterYForRow(dot.row)
        )
    }

    private fun startLineEndAnimation(
        state: DotState?,
        startX: Float, startY: Float, targetX: Float,
        targetY: Float
    ) {
        val valueAnimator = ValueAnimator.ofFloat(0f, 1f)
        valueAnimator.addUpdateListener { animation ->
            val t = animation.animatedValue as Float
            state!!.mLineEndX = (1 - t) * startX + t * targetX
            state.mLineEndY = (1 - t) * startY + t * targetY
            invalidate()
        }
        valueAnimator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                state!!.mLineAnimator = null
            }
        })
        valueAnimator.interpolator = mFastOutSlowInInterpolator
        valueAnimator.duration = pathEndAnimationDuration.toLong()
        valueAnimator.start()
        state!!.mLineAnimator = valueAnimator
    }

    private fun startSizeAnimation(
        start: Float, end: Float, duration: Long,
        interpolator: Interpolator?, state: DotState?,
        endRunnable: Runnable?
    ) {
        val valueAnimator = ValueAnimator.ofFloat(start, end)
        valueAnimator.addUpdateListener { animation ->
            state!!.mSize = animation.animatedValue as Float
            invalidate()
        }
        if (endRunnable != null) {
            valueAnimator.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    if (endRunnable != null) {
                        endRunnable.run()
                    }
                }
            })
        }
        valueAnimator.interpolator = interpolator
        valueAnimator.duration = duration
        valueAnimator.start()
    }

    /**
     * Helper method to map a given x, y to its corresponding cell
     *
     * @param x The x coordinate
     * @param y The y coordinate
     * @return
     */
    private fun checkForNewHit(x: Float, y: Float): Dot? {
        val rowHit = getRowHit(y)
        if (rowHit < 0) {
            return null
        }
        val columnHit = getColumnHit(x)
        if (columnHit < 0) {
            return null
        }
        return if (mPatternDrawLookup[rowHit][columnHit]) {
            null
        } else Dot.of(rowHit, columnHit)
    }

    /**
     * Helper method to find the row that y coordinate falls into
     *
     * @param y The y coordinate
     * @return The mRow that y falls in, or -1 if it falls in no mRow
     */
    private fun getRowHit(y: Float): Int {
        val squareHeight = mViewHeight
        val hitSize = squareHeight * mHitFactor
        val offset = paddingTop + (squareHeight - hitSize) / 2f
        for (i in 0 until sDotCount) {
            val hitTop = offset + squareHeight * i
            if (y >= hitTop && y <= hitTop + hitSize) {
                return i
            }
        }
        return -1
    }

    /**
     * Helper method to find the column x falls into
     *
     * @param x The x coordinate
     * @return The mColumn that x falls in, or -1 if it falls in no mColumn
     */
    private fun getColumnHit(x: Float): Int {
        val squareWidth = mViewWidth
        val hitSize = squareWidth * mHitFactor
        val offset = paddingLeft + (squareWidth - hitSize) / 2f
        for (i in 0 until sDotCount) {
            val hitLeft = offset + squareWidth * i
            if (x >= hitLeft && x <= hitLeft + hitSize) {
                return i
            }
        }
        return -1
    }

    private fun handleActionMove(event: MotionEvent) {
        val radius = mPathWidth.toFloat()
        val historySize = event.historySize
        mTempInvalidateRect.setEmpty()
        var invalidateNow = false
        for (i in 0 until historySize + 1) {
            val x = if (i < historySize) event.getHistoricalX(i) else event
                .x
            val y = if (i < historySize) event.getHistoricalY(i) else event
                .y
            val hitDot = detectAndAddHit(x, y)
            val patternSize = mPattern.size
            if (hitDot != null && patternSize == 1) {
                mPatternInProgress = true
                notifyPatternStarted()
            }
            // Note current x and y for rubber banding of in progress patterns
            val dx = Math.abs(x - mInProgressX)
            val dy = Math.abs(y - mInProgressY)
            if (dx > DEFAULT_DRAG_THRESHOLD || dy > DEFAULT_DRAG_THRESHOLD) {
                invalidateNow = true
            }
            if (mPatternInProgress && patternSize > 0) {
                val pattern = mPattern
                val lastDot = pattern[patternSize - 1]
                val lastCellCenterX = getCenterXForColumn(lastDot!!.column)
                val lastCellCenterY = getCenterYForRow(lastDot.row)

                // Adjust for drawn segment from last cell to (x,y). Radius
                // accounts for line width.
                var left = Math.min(lastCellCenterX, x) - radius
                var right = Math.max(lastCellCenterX, x) + radius
                var top = Math.min(lastCellCenterY, y) - radius
                var bottom = Math.max(lastCellCenterY, y) + radius

                // Invalidate between the pattern's new cell and the pattern's
                // previous cell
                if (hitDot != null) {
                    val width = mViewWidth * 0.5f
                    val height = mViewHeight * 0.5f
                    val hitCellCenterX = getCenterXForColumn(hitDot.column)
                    val hitCellCenterY = getCenterYForRow(hitDot.row)
                    left = Math.min(hitCellCenterX - width, left)
                    right = Math.max(hitCellCenterX + width, right)
                    top = Math.min(hitCellCenterY - height, top)
                    bottom = Math.max(hitCellCenterY + height, bottom)
                }

                // Invalidate between the pattern's last cell and the previous
                // location
                mTempInvalidateRect.union(
                    Math.round(left), Math.round(top),
                    Math.round(right), Math.round(bottom)
                )
            }
        }
        mInProgressX = event.x
        mInProgressY = event.y

        // To save updates, we only invalidate if the user moved beyond a
        // certain amount.
        if (invalidateNow) {
            mInvalidate.union(mTempInvalidateRect)
            invalidate(mInvalidate)
            mInvalidate.set(mTempInvalidateRect)
        }
    }

    private fun sendAccessEvent(resId: Int) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            contentDescription = context.getString(resId)
            sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_SELECTED)
            contentDescription = null
        } else {
            announceForAccessibility(context.getString(resId))
        }
    }

    private fun handleActionUp(event: MotionEvent) {
        // Report pattern detected
        if (!mPattern.isEmpty()) {
            mPatternInProgress = false
            cancelLineAnimations()
            notifyPatternDetected()
            invalidate()
        }
        if (PROFILE_DRAWING) {
            if (mDrawingProfilingStarted) {
                Debug.stopMethodTracing()
                mDrawingProfilingStarted = false
            }
        }
    }

    private fun cancelLineAnimations() {
        for (i in 0 until sDotCount) {
            for (j in 0 until sDotCount) {
                val state = mDotStates[i][j]
                if (state!!.mLineAnimator != null) {
                    state.mLineAnimator!!.cancel()
                    state.mLineEndX = Float.MIN_VALUE
                    state.mLineEndY = Float.MIN_VALUE
                }
            }
        }
    }

    private fun handleActionDown(event: MotionEvent) {
        resetPattern()
        val x = event.x
        val y = event.y
        val hitDot = detectAndAddHit(x, y)
        if (hitDot != null) {
            mPatternInProgress = true
            patternViewMode = PatternViewMode.CORRECT
            notifyPatternStarted()
        } else {
            mPatternInProgress = false
            notifyPatternCleared()
        }
        if (hitDot != null) {
            val startX = getCenterXForColumn(hitDot.column)
            val startY = getCenterYForRow(hitDot.row)
            val widthOffset = mViewWidth / 2f
            val heightOffset = mViewHeight / 2f
            invalidate(
                (startX - widthOffset).toInt(),
                (startY - heightOffset).toInt(),
                (startX + widthOffset).toInt(),
                (startY + heightOffset).toInt()
            )
        }
        mInProgressX = x
        mInProgressY = y
        if (PROFILE_DRAWING) {
            if (!mDrawingProfilingStarted) {
                Debug.startMethodTracing("PatternLockDrawing")
                mDrawingProfilingStarted = true
            }
        }
    }

    private fun getCenterXForColumn(column: Int): Float {
        return paddingLeft + column * mViewWidth + mViewWidth / 2f
    }

    private fun getCenterYForRow(row: Int): Float {
        return paddingTop + row * mViewHeight + mViewHeight / 2f
    }

    private fun calculateLastSegmentAlpha(
        x: Float, y: Float, lastX: Float,
        lastY: Float
    ): Float {
        val diffX = x - lastX
        val diffY = y - lastY
        val dist = Math.sqrt((diffX * diffX + diffY * diffY).toDouble()).toFloat()
        val fraction = dist / mViewWidth
        return Math.min(1f, Math.max(0f, (fraction - 0.3f) * 4f))
    }

    private fun getCurrentColor(partOfPattern: Boolean): Int {
        return if (!partOfPattern || isInStealthMode || mPatternInProgress) {
            normalStateColor
        } else if (patternViewMode == PatternViewMode.WRONG) {
            wrongStateColor
        } else if (patternViewMode == PatternViewMode.CORRECT
            || patternViewMode == PatternViewMode.AUTO_DRAW
        ) {
            correctStateColor
        } else {
            throw IllegalStateException("Unknown view mode " + patternViewMode)
        }
    }

    private fun drawCircle(
        canvas: Canvas, centerX: Float, centerY: Float,
        size: Float, partOfPattern: Boolean, alpha: Float
    ) {
        mDotPaint!!.color = getCurrentColor(partOfPattern)
        mDotPaint!!.alpha = (alpha * 255).toInt()
        canvas.drawCircle(centerX, centerY, size / 2, mDotPaint!!)
    }

    /**
     * Represents a cell in the matrix of the pattern view
     */
    class Dot : Parcelable {
        var row: Int
            private set
        var column: Int
            private set

        private constructor(row: Int, column: Int) {
            checkRange(row, column)
            this.row = row
            this.column = column
        }

        val id: Int
            /**
             * Gets the identifier of the dot. It is counted from left to right, top to bottom of the
             * matrix, starting by zero
             */
            get() = row * sDotCount + column

        override fun toString(): String {
            return "(Row = " + row + ", Col = " + column + ")"
        }

        override fun equals(`object`: Any?): Boolean {
            return if (`object` is Dot) (column == `object`.column
                    && row == `object`.row) else super.equals(`object`)
        }

        override fun hashCode(): Int {
            var result = row
            result = 31 * result + column
            return result
        }

        override fun describeContents(): Int {
            return 0
        }

        override fun writeToParcel(dest: Parcel, flags: Int) {
            dest.writeInt(column)
            dest.writeInt(row)
        }

        private constructor(`in`: Parcel) {
            column = `in`.readInt()
            row = `in`.readInt()
        }

        companion object {
            private var sDots: Array<Array<Dot?>>

            init {
                sDots = Array(sDotCount) { arrayOfNulls(sDotCount) }

                // Initializing the dots
                for (i in 0 until sDotCount) {
                    for (j in 0 until sDotCount) {
                        sDots[i][j] = Dot(i, j)
                    }
                }
            }

            /**
             * @param row    The mRow of the cell.
             * @param column The mColumn of the cell.
             */
            @Synchronized
            fun of(row: Int, column: Int): Dot? {
                checkRange(row, column)
                return sDots[row][column]
            }

            /**
             * Gets a cell from its identifier
             */
            @Synchronized
            fun of(id: Int): Dot? {
                return of(id / sDotCount, id % sDotCount)
            }

            private fun checkRange(row: Int, column: Int) {
                require(!(row < 0 || row > sDotCount - 1)) {
                    ("mRow must be in range 0-"
                            + (sDotCount - 1))
                }
                require(!(column < 0 || column > sDotCount - 1)) {
                    ("mColumn must be in range 0-"
                            + (sDotCount - 1))
                }
            }

            @JvmField
            val CREATOR: Parcelable.Creator<Dot> = object : Parcelable.Creator<Dot> {
                override fun createFromParcel(`in`: Parcel): Dot? {
                    return Dot(`in`)
                }

                override fun newArray(size: Int): Array<Dot?> {
                    return arrayOfNulls(size)
                }
            }
        }
    }

    /**
     * The parcelable for saving and restoring a lock pattern view
     */
    private class SavedState : BaseSavedState {
        val serializedPattern: String?
        val displayMode: Int
        val isInputEnabled: Boolean
        val isInStealthMode: Boolean
        val isTactileFeedbackEnabled: Boolean

        /**
         * Constructor called from [PatternLockView.onSaveInstanceState]
         */
        constructor(
            superState: Parcelable?, serializedPattern: String,
            displayMode: Int, inputEnabled: Boolean, inStealthMode: Boolean,
            tactileFeedbackEnabled: Boolean
        ) : super(superState) {
            this.serializedPattern = serializedPattern
            this.displayMode = displayMode
            isInputEnabled = inputEnabled
            isInStealthMode = inStealthMode
            isTactileFeedbackEnabled = tactileFeedbackEnabled
        }

        /**
         * Constructor called from [.CREATOR]
         */
        private constructor(`in`: Parcel) : super(`in`) {
            serializedPattern = `in`.readString()
            displayMode = `in`.readInt()
            isInputEnabled = (`in`.readValue(null) as Boolean?)!!
            isInStealthMode = (`in`.readValue(null) as Boolean?)!!
            isTactileFeedbackEnabled = (`in`.readValue(null) as Boolean?)!!
        }

        override fun writeToParcel(dest: Parcel, flags: Int) {
            super.writeToParcel(dest, flags)
            dest.writeString(serializedPattern)
            dest.writeInt(displayMode)
            dest.writeValue(isInputEnabled)
            dest.writeValue(isInStealthMode)
            dest.writeValue(isTactileFeedbackEnabled)
        }

        companion object {
            @Suppress("unused")
            @JvmField
            val CREATOR: Parcelable.Creator<SavedState> = object : Parcelable.Creator<SavedState> {
                override fun createFromParcel(`in`: Parcel): SavedState? {
                    return SavedState(`in`)
                }

                override fun newArray(size: Int): Array<SavedState?> {
                    return arrayOfNulls(size)
                }
            }
        }
    }

    class DotState {
        var mScale = 1.0f
        var mTranslateY = 0.0f
        var mAlpha = 1.0f
        var mSize = 0f
        var mLineEndX = Float.MIN_VALUE
        var mLineEndY = Float.MIN_VALUE
        var mLineAnimator: ValueAnimator? = null
    }

    companion object {
        private const val DEFAULT_PATTERN_DOT_COUNT = 3
        private const val PROFILE_DRAWING = false

        /**
         * The time (in millis) spend in animating each circle of a lock pattern if
         * the animating mode is set. The entire animation should take this constant
         * the length of the pattern to complete.
         */
        private const val MILLIS_PER_CIRCLE_ANIMATING = 700

        // Amount of time (in millis) spent to animate a dot
        private const val DEFAULT_DOT_ANIMATION_DURATION = 190

        // Amount of time (in millis) spent to animate a path ends
        private const val DEFAULT_PATH_END_ANIMATION_DURATION = 100

        // This can be used to avoid updating the display for very small motions or noisy panels
        private const val DEFAULT_DRAG_THRESHOLD = 0.0f

        // Made static so that the static inner class can use it
        private var sDotCount = 0
    }
}
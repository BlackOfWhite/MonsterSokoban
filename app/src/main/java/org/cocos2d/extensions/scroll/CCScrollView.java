package org.cocos2d.extensions.scroll;

import android.R.bool;
import android.view.MotionEvent;
import com.game.helpers.InGameHelper;
import org.cocos2d.actions.base.CCFiniteTimeAction;
import org.cocos2d.actions.instant.CCCallFunc;
import org.cocos2d.actions.interval.CCMoveTo;
import org.cocos2d.actions.interval.CCSequence;
import org.cocos2d.events.CCTouchDispatcher;
import org.cocos2d.layers.CCLayer;
import org.cocos2d.nodes.CCDirector;
import org.cocos2d.nodes.CCNode;
import org.cocos2d.types.CGPoint;
import org.cocos2d.types.CGRect;
import org.cocos2d.types.CGSize;

public class CCScrollView extends CCLayer {

  public static final int CCScrollViewDirectionHorizontal = 1;
  public static final int CCScrollViewDirectionVertical = 2;
  public static final int CCScrollViewDirectionBoth = 3;
  private static final float SCROLL_DEACCELERATION_RATE = 0.6f; // def 0.95
  private static final float SCROLL_DEACCELERATION_DIST = 0.66f; // def 1
  private static final float BOUNCE_DURATION = 0.35f;
  private static final float INSET_RATIO = 0.3f;
  // Update.
  private static CGPoint startLocation;
  public boolean bounces;
  public CGSize viewSize;
  // ArrayList<MotionEvent> touches_;
  public int direction;
  public CCScrollViewDelegate delegate;
  public bool isDragging;
  // Paging
  CGSize size;
  boolean isPagingEnabled;
  int currentPage;
  boolean isScrollLock;
  boolean touchMoved_;
  boolean isDragging_;
  final CCClipNode container_;
  CGPoint maxInset_;
  CGPoint minInset_;
  CGPoint scrollDistance_;
  CGPoint touchPoint_;
  // float minScale_, maxScale_;
  float touchLength_;
  private boolean clipsToBounds;

  // @property (nonatomic, assign) CGFloat zoomScale;
  // @property (nonatomic, assign) CGFloat minZoomScale;
  // @property (nonatomic, assign) CGFloat maxZoomScale;

  // public CGPoint contentOffset;

  public CCScrollView(CGSize size) {
    viewSize = size;

    container_ = new CCClipNode();
    // touches_ = new ArrayList<MotionEvent>();
    delegate = null;
    bounces = true;
    container_.setContentSize(CGSize.zero());
    direction = CCScrollViewDirectionBoth;
    container_.setPosition(CGPoint.ccp(0.0f, 0.0f));
    touchLength_ = 0.0f;

    setIsTouchEnabled(true);
    setClipToBounds(true);
    addChild(container_);
    // minScale_ = maxScale_ = 1.0f;
    currentPage = 0;
  }

  public static CCScrollView view(CGSize size) {
    return new CCScrollView(size);
  }

  @Override
  public void registerWithTouchDispatcher() {
    CCTouchDispatcher.sharedDispatcher().addTargetedDelegate(this, 0, false);
  }

  public void setClipToBounds(boolean bClip) {
    if (bClip != clipsToBounds) {
      CGRect clipRect;
      if (bClip) {
        clipRect = CGRect.make(0, 0, viewSize.width, viewSize.height);
      } else {
        clipRect = CGRect.make(CCClipNode.RECT_ORIGIN_INVALID, 0, 0, 0);
      }
      clipsToBounds = bClip;
      container_.setClipRect(clipRect);
    }
  }

  public boolean isPagingEnabled() {
    return isPagingEnabled;
  }

  public void setPagingEnabled(boolean set) {
    isPagingEnabled = set;
  }

  public CGSize getPageSize() {
    return size;
  }

  public void setPageSize(CGSize pageSize) {
    size = pageSize;
  }

  public int getCurrentPageNum() {
    return currentPage;
  }

  public void setCurrentPageNum(int page) {
    this.currentPage = page;
  }

  public boolean isNodeVisible(CCNode node) {
    CGPoint offset = contentOffset();
    CGSize size = viewSize;
    float scale = 1.0f;// zoomScale;

    CGRect viewRect;

    viewRect = CGRect.make(-offset.x / scale, -offset.y / scale, size.width / scale, size.height / scale);

    return CGRect.intersects(viewRect, getBoundingBox());
  }

  /**
   * Scroll page to next one. Maximum page is total number of items in shop -
   * 1.
   */
  public void scrollToNextPage(float pageWidth, int totalPages) {
    int page = getCurrentPageNum();
    if (currentPage < totalPages - 1 && numberOfRunningActions() == 0) {
      setContentOffset(CGPoint.ccp((currentPage + 1) * -pageWidth, 0));
      setCurrentPageNum(currentPage + 1);
    }
  }

  /**
   * Scroll page to previous one. Minimum page is 0.
   */
  public void scrollToPreviousPage(float pageWidth) {
    if (currentPage > 0 && numberOfRunningActions() == 0) {
      setContentOffset(CGPoint.ccp((currentPage - 1) * -pageWidth, 0));
      setCurrentPageNum(currentPage - 1);
    }
  }

  public void setIsTouchEnabled(boolean e) {
    super.setIsTouchEnabled(e);
    if (!e) {
      isDragging_ = false;
      touchMoved_ = false;
      // touches_.clear();
    }
  }

  public void setIsScrollLock(boolean e) {
    isScrollLock = e;
  }

  public void setContentOffset(CGPoint offset) {
    setContentOffset(offset, false);
  }

  public void setContentOffset(CGPoint offset, boolean animated) {
    if (animated) { // animate scrolling
      setContentOffset(offset, BOUNCE_DURATION);
    } else { // set the container position directly
      if (!bounces) {
        CGPoint minOffset = minContainerOffset();
        CGPoint maxOffset = maxContainerOffset();

        offset.x = Math.max(minOffset.x, Math.min(maxOffset.x, offset.x));
        offset.y = Math.max(minOffset.y, Math.min(maxOffset.y, offset.y));
      }
      container_.setPosition(offset);
      if (delegate != null) {
        delegate.scrollViewDidScroll(this);
      }
    }
  }

  public void setContentOffset(CGPoint offset, float dt) {
    CCFiniteTimeAction scroll, expire;

    scroll = CCMoveTo.action(dt, offset);
    expire = CCCallFunc.action(this, "stoppedAnimatedScroll");
    container_.runAction(CCSequence.actions(scroll, expire));
    schedule("performedAnimatedScroll");
  }

  public CGPoint contentOffset() {
    return container_.getPosition();
  }
  // -(void)setZoomScale:(float)s animated:(BOOL)animated;
  // -(void)setZoomScale:(float)s animatedInDuration:(ccTime)dt;

  public void setViewSize(CGSize size) {
    if (!CGSize.equalToSize(viewSize, size)) {
      viewSize = size;
      computeInsets();

      CGRect clipRect;
      if (clipsToBounds) {
        clipRect = CGRect.make(0, 0, viewSize.width, viewSize.height);
        container_.setClipRect(clipRect);
      }
    }
  }

  public void computeInsets() {
    maxInset_ = maxContainerOffset();
    maxInset_ = CGPoint.ccp(maxInset_.x + viewSize.width * INSET_RATIO,
        maxInset_.y + viewSize.height * INSET_RATIO);
    minInset_ = minContainerOffset();
    minInset_ = CGPoint.ccp(minInset_.x - viewSize.width * INSET_RATIO,
        minInset_.y - viewSize.height * INSET_RATIO);
  }

  public void relocateContainer(boolean animated) {
    CGPoint oldPoint, min, max;
    float newX, newY;

    min = minContainerOffset();
    max = maxContainerOffset();

    oldPoint = container_.getPosition();
    newX = oldPoint.x;
    newY = oldPoint.y;

    // 4PAGING -- GOOD
    if (isPagingEnabled) {
      int verticalPageNum = Math.round(newY / viewSize.height);
      int horizontalPageNum = Math.round(newX / viewSize.width);
      if (direction == CCScrollViewDirectionBoth || direction == CCScrollViewDirectionHorizontal) {
        newX = horizontalPageNum * viewSize.width;
      }

      if (direction == CCScrollViewDirectionBoth || direction == CCScrollViewDirectionVertical) {
        newY = verticalPageNum * viewSize.height;
      }
      currentPage = -horizontalPageNum;
    }

    if (direction == CCScrollViewDirectionBoth || direction == CCScrollViewDirectionHorizontal) {
      newX = Math.min(newX, max.x);
      newX = Math.max(newX, min.x);
    }
    if (direction == CCScrollViewDirectionBoth || direction == CCScrollViewDirectionVertical) {
      newY = Math.min(newY, max.y);
      newY = Math.max(newY, min.y);
    }
    if (newY != oldPoint.y || newX != oldPoint.x) {
      setContentOffset(CGPoint.ccp(newX, newY), animated);
    }
  }

  public CGPoint maxContainerOffset() {
    return CGPoint.ccp(0.0f, 0.0f);
  }

  public CGPoint minContainerOffset() {
    return CGPoint.ccp(viewSize.width - container_.getContentSize().width * container_.getScaleX(),
        viewSize.height - container_.getContentSize().height * container_.getScaleY());

  }

  public void deaccelerateScrolling(float dt) {
    if (isDragging_) {
      unschedule("deaccelerateScrolling");
      return;
    }

    float newX, newY;
    CGPoint maxInset, minInset;

    container_.setPosition(CGPoint.ccpAdd(container_.getPosition(), scrollDistance_));

    if (bounces) {
      maxInset = maxInset_;
      minInset = minInset_;
    } else {
      maxInset = maxContainerOffset();
      minInset = minContainerOffset();
    }

    // check to see if offset lies within the inset bounds
    newX = Math.min(container_.getPosition().x, maxInset.x);
    newX = Math.max(newX, minInset.x);
    newY = Math.min(container_.getPosition().y, maxInset.y);
    newY = Math.max(newY, minInset.y);

    // 4PAGING - USEFUL?
    if (isPagingEnabled) {
      int horizontalPageNum = Math.round(newX / viewSize.width);
      float endX = 1;
      if (direction == CCScrollViewDirectionBoth || direction == CCScrollViewDirectionHorizontal) {
        endX = horizontalPageNum * viewSize.width;
      }
      horizontalPageNum = Math.round(endX / viewSize.width);
      currentPage = -horizontalPageNum;
    }

    scrollDistance_ = CGPoint.ccpSub(scrollDistance_,
        CGPoint.ccp(newX - container_.getPosition().x, newY - container_.getPosition().y));
    scrollDistance_ = CGPoint.ccpMult(scrollDistance_, SCROLL_DEACCELERATION_RATE);
    setContentOffset(CGPoint.ccp(newX, newY));

    if (CGPoint.ccpLengthSQ(scrollDistance_) <= SCROLL_DEACCELERATION_DIST * SCROLL_DEACCELERATION_DIST || newX == maxInset.x
        || newX == minInset.x || newY == maxInset.y || newY == minInset.y) {
      unschedule("deaccelerateScrolling");
      relocateContainer(true);
    }
  }

  public void stoppedAnimatedScroll() {
    unschedule("performedAnimatedScroll");
  }

  public void performedAnimatedScroll(float dt) {
    if (isDragging_) {
      unschedule("performedAnimatedScroll");
      return;
    }
    if (delegate != null) {
      delegate.scrollViewDidScroll(this);
    }
  }

  @Override
  public CGSize getContentSize() {
    return CGSize.make(container_.getContentSize().width, container_.getContentSize().height);
  }

  @Override
  public void setContentSize(CGSize size) {
    if (container_ == null) {
      super.setContentSize(size);
    } else {
      container_.setContentSize(size);
      maxInset_ = maxContainerOffset();
      maxInset_ = CGPoint.ccp(maxInset_.x + viewSize.width * INSET_RATIO,
          maxInset_.y + viewSize.height * INSET_RATIO);
      minInset_ = minContainerOffset();
      minInset_ = CGPoint.ccp(minInset_.x - viewSize.width * INSET_RATIO,
          minInset_.y - viewSize.height * INSET_RATIO);
    }
  }

  /**
   * make sure all children go to the container
   */
  @Override
  public CCNode addChild(CCNode node, int z, int aTag) {
    // node.isRelativeAnchorPoint = true;
    node.setAnchorPoint(CGPoint.ccp(0.0f, 0.0f));
    if (container_ != node) {
      container_.addChild(node, z, aTag);
    } else {
      super.addChild(node, z, aTag);
    }
    return this;
  }

  // public void beforeDraw(GL10 gl) {
  // if (clipsToBounds) {
  // CGSize size = CCDirector.sharedDirector().winSize();
  // gl.glEnable(GL10.GL_SCISSOR_TEST);
  // CGPoint pos = getPosition();
  // pos = getParent().convertToWorldSpace(pos.x, pos.y);
  // pos.y = size.height - pos.y;
  // pos = CCDirector.sharedDirector().convertToUI(pos);
  //
  // CGPoint pos2 = getPosition();
  // pos2 = CGPoint.ccpAdd(pos2, CGPoint.ccp(viewSize.width,
  // viewSize.height));
  // pos2 = getParent().convertToWorldSpace(pos2.x, pos2.y);
  // pos2.y = size.height - pos2.y;
  // pos2 = CCDirector.sharedDirector().convertToUI(pos2);
  // float x1 = Math.min(pos.x, pos2.x);
  // float y1 = Math.min(pos.y, pos2.y);
  // float x2 = Math.max(pos.x, pos2.x);
  // float y2 = Math.max(pos.y, pos2.y);
  // gl.glScissor((int)x1, (int)y1, (int)(x2 - x1), (int)(y2 - y1));
  // }
  // }

  /**
   * retract what's done in beforeDraw so that there's no side effect to other
   * nodes.
   */
  // public void afterDraw(GL10 gl) {
  // if (clipsToBounds) {
  // gl.glDisable(GL10.GL_SCISSOR_TEST);
  // }
  // }

  // @Override
  // public void visit(GL10 gl) {
  // if (!visible_)
  // return;
  //
  // beforeDraw(gl);
  // super.visit(gl);
  // afterDraw(gl);
  // }

  //
  // dispatch events
  //
  @Override
  public boolean ccTouchesBegan(MotionEvent event) {
    if (!getVisible()) {
      return false;
    }

    // Return false if click detected!
    startLocation = CCDirector.sharedDirector().convertToGL(CGPoint.ccp(event.getX(), event.getY()));

    CGRect frame;

    frame = CGRect.make(0, 0, viewSize.width, viewSize.height);
    // dispatcher does not know about clipping. reject touches outside
    // visible bounds.
    CGPoint touch = convertTouchToNodeSpace(event);
    // if (touches_.size() > 2 ||
    // touchMoved_ ||
    // !CGRect.containsPoint(frame, container_.convertToWorldSpace(touch.x,
    // touch.y))) {
    if (!CGRect.containsPoint(frame, touch)) {
      touchPoint_ = CGPoint.ccp(-1.0f, -1.0f);
      isDragging_ = false;
      return false;
    }

    // if (!touches_.contains(event)) {
    // touches_.add(event);
    // }
    // if (touches_.size() == 1) { // scrolling
    touchPoint_ = touch;// convertTouchToNodeSpace(event);
    touchMoved_ = false;
    isDragging_ = true; // dragging started
    scrollDistance_ = CGPoint.ccp(0.0f, 0.0f);
    touchLength_ = 0.0f;

    // } else if (touches_.size() == 2) {
    // /*
    // touchPoint_ =
    // CGPoint.ccpMidpoint(convertTouchToNodeSpace(touches_.get(0)),
    // convertTouchToNodeSpace(touches_.get(1));
    // touchLength_ =
    // CGPoint.ccpDistance(convertTouchToNodeSpace(touches_.get(0)),
    // convertTouchToNodeSpace(touches_.get(1));
    // */
    // isDragging_ = false;
    // }

    return true;
  }

  @Override
  public boolean ccTouchesMoved(MotionEvent event) {
    if (!getVisible() || isScrollLock) {
      return false;
    }
    touchMoved_ = true;
    // if (touches_.contains(event)) {
    // if (touches_.size() == 1 && isDragging_) { // scrolling
    if (isDragging_) { // scrolling
      CGPoint moveDistance, newPoint;
      CGRect frame;
      float newX, newY;

      frame = CGRect.make(0, 0, viewSize.width, viewSize.height);
      newPoint = convertTouchToNodeSpace(event);
      moveDistance = CGPoint.ccpSub(newPoint, touchPoint_);
      touchPoint_ = newPoint;

      if (CGRect.containsPoint(frame, newPoint)) {
        switch (direction) {
          case CCScrollViewDirectionVertical:
            moveDistance = CGPoint.ccp(0.0f, moveDistance.y);
            break;
          case CCScrollViewDirectionHorizontal:
            moveDistance = CGPoint.ccp(moveDistance.x, 0.0f);
            break;
          default:
            break;
        }
        container_.setPosition(CGPoint.ccpAdd(container_.getPosition(), moveDistance));

        // check to see if offset lies within the inset bounds
        newX = Math.min(container_.getPosition().x, maxInset_.x);
        newX = Math.max(newX, minInset_.x);
        newY = Math.min(container_.getPosition().y, maxInset_.y);
        newY = Math.max(newY, minInset_.y);

        // 4PAGING - USEFUL?
        // if (isPagingEnabled)
        // {
        // int horizontalPageNum = Math.round(newX / viewSize.width);
        // float endX=1;
        // if (direction == CCScrollViewDirectionBoth || direction ==
        // CCScrollViewDirectionHorizontal){
        // endX = horizontalPageNum * viewSize.width;
        // }
        // horizontalPageNum = Math.round(endX / viewSize.width);
        // currentPage=-horizontalPageNum;
        // }

        scrollDistance_ = CGPoint.ccpSub(moveDistance,
            CGPoint.ccp(newX - container_.getPosition().x, newY - container_.getPosition().y));
        setContentOffset(CGPoint.ccp(newX, newY));
      }

      return false;
    }
    // else if (touches_.size() == 2 && !isDragging_) {
    // touchMoved_ = true;
    // const CGFloat len = ccpDistance([container_
    // convertTouchToNodeSpace:[touches_ objectAtIndex:0]],
    // [container_ convertTouchToNodeSpace:[touches_ objectAtIndex:1]]);
    // [self setZoomScale:self.zoomScale*len/touchLength_];
    // }
    // }

    return true;
  }

  @Override
  public boolean ccTouchesEnded(MotionEvent event) {
    if (!getVisible()) {
      return false;
    }

    // Update.
    CGPoint endLocation = CCDirector.sharedDirector().convertToGL(CGPoint.ccp(event.getX(), event.getY()));
    if (startLocation != null && InGameHelper.detectClick(startLocation, endLocation)) {
      return false;
    }

    // if (touches_.contains(event)) {
    // if (touches_.size() == 1 && touchMoved_) {
    if (touchMoved_) {
      schedule("deaccelerateScrolling");
    }
    // touches_.clear();
    // }
    // if (touches_.size() == 0) {
    isDragging_ = false;
    touchMoved_ = false;
    // }

    return true;
  }

  @Override
  public boolean ccTouchesCancelled(MotionEvent event) {
    if (!getVisible()) {
      return false;
    }
    // touches_.remove(event);
    // if (touches_.size() == 0) {
    isDragging_ = false;
    touchMoved_ = false;
    // }

    return true;
  }

}

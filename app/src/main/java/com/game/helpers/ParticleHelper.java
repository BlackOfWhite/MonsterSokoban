package com.game.helpers;

import com.game.preferences.SpritePreferences;
import org.cocos2d.nodes.CCTextureCache;
import org.cocos2d.particlesystem.CCParticleExplosion;
import org.cocos2d.particlesystem.CCParticleSystem;
import org.cocos2d.types.CGPoint;
import org.cocos2d.types.ccColor4F;

/**
 * Class for managing particle emitters.
 *
 * @author niewinskip
 */
public final class ParticleHelper {

  /**
   * Private constructor.
   */
  private ParticleHelper() {

  }

  /**
   * Create glowing emitter. Caution, must be declared this class.
   *
   * @param xPosition X position.
   * @param yPosition Y position.
   * @param radius Radius.
   * @return CCParticleSystem.
   */
  public static CCParticleSystem gasEmitter(float xPosition, float yPosition, float radius) {
    CCParticleSystem gasEmitter = CCParticleExplosion.node();
    CCTextureCache sharedTexture = CCTextureCache.sharedTextureCache();
    gasEmitter.setTexture(sharedTexture.addImage(SpritePreferences.FIRE_PARTICLE));
    gasEmitter.resetSystem();
    gasEmitter.setAutoRemoveOnFinish(true);
    gasEmitter.setStartColor(new ccColor4F(0.2f, 1f, 0.2f, 1f));
    gasEmitter.setEndColor(new ccColor4F(0.2f, 0.8f, 0.2f, 1f));
    gasEmitter.setStartColorVar(new ccColor4F(0.1f, 0.1f, 0.1f, 0.5f));
    gasEmitter.setEndColorVar(new ccColor4F(0, 0, 0, 0));
    gasEmitter.setStartSize(radius * 0.5f);
    gasEmitter.setStartSizeVar(radius * 0.125f);
    gasEmitter.setEndSize(-1);
    gasEmitter.setVisible(true);
    gasEmitter.setPosition(xPosition, yPosition);
    gasEmitter.setPosVar(CGPoint.ccp(radius, radius));
    gasEmitter.setDuration(-1);
    gasEmitter.setLife(3f);
    gasEmitter.setLifeVar(0.2f);
    gasEmitter.setEmissionRate(radius * radius * 0.001f);
    gasEmitter.setAngle(180f);
    gasEmitter.setAngleVar(180f);
    gasEmitter.setEmitterMode(0);
    gasEmitter.setBlendAdditive(false);
    gasEmitter.setSpeed(radius * 0.125f);
    gasEmitter.setPositionType(CCParticleSystem.kCCPositionTypeFree);
//		gasEmitter.setBlendFunc(new ccBlendFunc(771, 770));
    return gasEmitter;
  }

  /**
   * Create portal emitter.
   *
   * @param xPosition X position.
   * @param yPosition Y position.
   * @param radius Tile side size.
   * @return CCParticleSystem.
   */
  public static CCParticleSystem portalEmitter(float xPosition, float yPosition, float radius) {
    CCParticleSystem portalEmitter = CCParticleExplosion.node();
    CCTextureCache sharedTexture = CCTextureCache.sharedTextureCache();
    portalEmitter.setTexture(sharedTexture.addImage(SpritePreferences.STAR_PARTICLE));
    portalEmitter.resetSystem();
    portalEmitter.setAutoRemoveOnFinish(true);
    portalEmitter.setEmitterMode(CCParticleSystem.kCCParticleModeRadius);
    portalEmitter.setStartColor(new ccColor4F(0.3f, 0.2f, 1f, 1f));
    portalEmitter.setEndColor(new ccColor4F(0.6f, 0.2f, 1f, 1f));
    portalEmitter.setStartColorVar(new ccColor4F(0.3f, 0.3f, 0.3f, 0.5f));
    portalEmitter.setEndColorVar(new ccColor4F(0, 0, 0, 0));
    portalEmitter.setStartSize(radius * 0.1f);
    portalEmitter.setEndSize(-1);
    portalEmitter.setVisible(true);
//		portalEmitter.setScale(0f);
    portalEmitter.setPosition(xPosition, yPosition);
    portalEmitter.setStartRadius(radius * 0.35f);
//		portalEmitter.setStartRadiusVar(radius * 0.04f);
    portalEmitter.setDuration(-1);
    portalEmitter.setLife(0.4f);
    portalEmitter.setLifeVar(0.05f);
    portalEmitter.setEmissionRate(radius * radius * 0.002f);
    portalEmitter.setAngle(90f);
    portalEmitter.setAngleVar(180);
    portalEmitter.setEmitterMode(1);
    portalEmitter.setBlendAdditive(true);
    return portalEmitter;
  }

  /**
   * Create fireflies emitter.
   *
   * @param areaWidth Area width to spawn particles.
   * @param areaHeight Area height to spawn particles.
   * @return CCParticleSystem.
   */
  public static CCParticleSystem fireflies(float areaWidth, float areaHeight) {
    CCParticleSystem firefliesEmitter = CCParticleExplosion.node();
    CCTextureCache sharedTexture = CCTextureCache.sharedTextureCache();
    firefliesEmitter.setTexture(sharedTexture.addImage(SpritePreferences.STAR_PARTICLE));
    ccColor4F ff = new ccColor4F(1f, 0.8f, 0.2f, 1f);
    firefliesEmitter.setStartColor(ff);
    firefliesEmitter.setEndColor(ff);
    firefliesEmitter.setStartColorVar(new ccColor4F(0.2f, 0.2f, 0.2f, 0.2f));
    firefliesEmitter.setEndColorVar(new ccColor4F(0, 0, 0, 0));
    firefliesEmitter.setVisible(true);
    firefliesEmitter.resetSystem();
    firefliesEmitter.setScale(2.0f);
    firefliesEmitter.setPosition(areaWidth / 2.0f, areaHeight / 2.0f);
    firefliesEmitter.setPosVar(CGPoint.ccp(areaWidth, areaHeight));
    firefliesEmitter.setDuration(-1);
    firefliesEmitter.setLife(5f);
    firefliesEmitter.setLifeVar(2f);
    firefliesEmitter.setEmissionRate(5);
    firefliesEmitter.setAngle(0f);
    firefliesEmitter.setAngleVar(180);
    firefliesEmitter.setEmitterMode(0);
    firefliesEmitter.setSpeed(45);
    firefliesEmitter.setSpeedVar(15);
    firefliesEmitter.setBlendAdditive(true);
    firefliesEmitter.setStartSize(areaHeight * 0.02f);
    firefliesEmitter.setStartSizeVar(areaHeight * 0.005f);
    // ONE
    // GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA
    // GL_SRC_ALPHA, GL_ONE
    // fireEmitter.setBlendFunc(new ccBlendFunc(GLES30.GL_ONE,
    // GLES30.GL_ONE_MINUS_SRC_ALPHA));
    firefliesEmitter.setAutoRemoveOnFinish(true);
    return firefliesEmitter;
  }
}

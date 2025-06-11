import ch.hevs.gdx2d.components.audio.MusicPlayer
import characters.{Hero, NPC}
import com.badlogic.gdx.Input

import java.io.File
import java.util
import scala.collection.mutable.ArrayBuffer
import scala.util.Random

class AttackManager(hero: Hero) {

  def handleHeroAttack(npcs: ArrayBuffer[NPC], keyStatus: util.Map[Integer, Boolean]): Unit = {
    if((keyStatus.get(Input.Keys.SPACE) || keyStatus.get(Input.Keys.ENTER))
      && !hero.isMoving) hero.attack()
    if(hero.isAttacking) {
      for (npc <- npcs) {
        if(Math.abs(npc.getPosition.x - hero.getPosition.x) <= hero.getAttackRange*48
          && Math.abs(npc.getPosition.y - hero.getPosition.y) <= hero.getAttackRange*48
          && !npc.isInvincible) {
          npc.takeDamage(5)
          if(!npc.isAlive) {
            hero.updateScore(npc.killPoints)
          }
        }
      }
    }
  }

}

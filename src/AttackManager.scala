import characters.{Evil, Hero, NPC}
import com.badlogic.gdx.Input

import java.util
import scala.collection.mutable.ArrayBuffer

class AttackManager(hero: Hero) {

  def handleHeroAttack(npcs: ArrayBuffer[NPC], keyStatus: util.Map[Integer, Boolean]): Unit = {
    if((keyStatus.get(Input.Keys.SPACE) || keyStatus.get(Input.Keys.ENTER))
      && !hero.isMoving) hero.attack()
    if(hero.codeLaser != null) {
      for (npc <- npcs) {
        if((Math.abs(npc.getPosition.x - hero.codeLaser.getPosition.x) <= 24 && npc.getPosition.y == hero.codeLaser.getPosition.y)
        || (Math.abs(npc.getPosition.y - hero.codeLaser.getPosition.y) <= 24 && npc.getPosition.x == hero.codeLaser.getPosition.x)) {
          if(!npc.isInvincible) {
            npc.takeDamage(5)
            hero.codeLaser = null
            if (!npc.isAlive) {
              hero.updateScore(npc.killPoints)
            }
            return
          }
        }
      }
    }
  }

  def handleNPCAttack(npcs: ArrayBuffer[NPC]): Boolean = {
    if(!hero.isAlive) return true
    for(npc <- npcs.filter(n => n.isInstanceOf[Evil])) {
      // Does NPC has to attck now ?
      if(!npc.isMoving && !npc.isInvincible &&
        (Math.abs(npc.getPosition.x - hero.getPosition.x) <= npc.getAttackRange*48
        && Math.abs(npc.getPosition.y - hero.getPosition.y) <= npc.getAttackRange*48)) npc.attack()
      // If he's attacking
      if(npc.isAttacking) {
        if(Math.abs(npc.getPosition.x - hero.getPosition.x) <= npc.getAttackRange*48
          && Math.abs(npc.getPosition.y - hero.getPosition.y) <= npc.getAttackRange*48
          && !hero.isInvincible) {
          hero.takeDamage(5)
          if(!hero.isAlive) return true // Handle hero death
        }
      }
    }
    false
  }

}

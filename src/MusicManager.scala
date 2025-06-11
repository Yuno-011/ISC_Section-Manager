import ch.hevs.gdx2d.components.audio.{MusicPlayer, SoundSample}

import java.io.File
import scala.collection.mutable.ArrayBuffer
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Random

class MusicManager {
  private var songsToPlay: ArrayBuffer[String] = ArrayBuffer()
  private var soundPlayer: MusicPlayer = null
  private var musicPlayer: MusicPlayer = null

  def playSound(soundPath: String): Unit = {
    songsToPlay += soundPath
    if(soundPlayer == null || !soundPlayer.isPlaying) playNextSoundNow()
    else waitUntilSoundFinished()
  }

  def playMusic(musicPath: String): Unit = {
    if(musicPlayer == null || !musicPlayer.isLooping) {
      musicPlayer = new MusicPlayer(musicPath)
      musicPlayer.loop()
    }
  }

  def stopMusic(): Unit = musicPlayer.stop()

  def getRandomKillPhrase: String = {
    var filename: String = ""
    val dir = new File("data/sounds/mudry")
    if (dir.exists && dir.isDirectory) {
      val files = dir.listFiles().filter(_.isFile)
      if (files.nonEmpty) filename = s"data/sounds/mudry/" + files(Random.nextInt(files.length)).getName
    }
    filename
  }

  def dispose(): Unit = {
    stopMusic()
    soundPlayer.dispose()
    musicPlayer.dispose()
  }

  private def waitUntilSoundFinished(): Unit = {
    Future {
      Thread.sleep(500)
      if(soundPlayer.isPlaying) waitUntilSoundFinished()
      else playNextSoundNow()
    }
  }

  private def playNextSoundNow(): Unit = {
    if(songsToPlay.nonEmpty) {
      soundPlayer = new MusicPlayer(songsToPlay.head)
      soundPlayer.play()
      songsToPlay = songsToPlay.tail
    }
  }
}

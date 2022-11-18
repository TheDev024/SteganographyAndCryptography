package cryptography

import java.awt.Color
import java.io.File
import java.util.*
import javax.imageio.ImageIO
import kotlin.experimental.xor
import kotlin.math.pow

/**
 * Java scanner
 */
val scanner: Scanner = Scanner(System.`in`)

/**
 * Cryptography and Steganography message encoder and decoder
 */
class SteganographyAndCryptography {

    /**
     * class cmd controller
     */
    fun cmd() {
        while (true) {
            println("Task (hide, show, exit):")
            when (val input = readln()) {
                "hide" -> hide()

                "show" -> show()

                "exit" -> break

                else -> println("Wrong task: $input")
            }
        }

        println("Bye!")
    }

    /**
     * Gets path of input image and decodes message from it
     */
    private fun show() {
        println("Input image file:")
        val imgPath = scanner.nextLine()

        println("Password:")
        val password = scanner.nextLine().encodeToByteArray()

        val file = File(imgPath)
        val image = ImageIO.read(file)

        var encryptedMessage = byteArrayOf()
        val byte = mutableListOf<Int>()

        repeat(image.height) imageIterator@{ y ->
            repeat(image.width) { x ->
                val pixel = image.getRGB(x, y)
                val bit = Color(pixel).blue % 2
                byte.add(bit)
                if (byte.size == 8) {
                    var newByte = 0
                    for (b in byte.indices) newByte += byte.reversed()[b] * 2.0.pow(b).toInt()
                    encryptedMessage = encryptedMessage.plus(newByte.toByte())
                    byte.clear()
                }
            }
        }
        for (i in 0..encryptedMessage.lastIndex - 2) if (encryptedMessage.slice(i..i + 2) == listOf<Byte>(
                0, 0, 3
            )
        ) {
            encryptedMessage = encryptedMessage.slice(0 until i).toByteArray()
            break
        }

        val decryptedBytes = mutableListOf<Byte>()

        for ((index, encryptedByte) in encryptedMessage.withIndex()) {
            val decryptedByte = password[index % password.size] xor encryptedByte
            decryptedBytes.add(decryptedByte)
        }

        val message = decryptedBytes.toByteArray().toString(Charsets.UTF_8)
        println("Message:\n$message")
    }

    /**
     * Gets path of input image and encodes the message into it
     */
    private fun hide() {
        println("Input image file:")
        val inputImg = scanner.nextLine()

        println("Output image file:")
        val outputImg = scanner.nextLine()
        val inputFile = File(inputImg)
        if (inputFile.exists()) {
            println("Message to hide:")
            val message = scanner.nextLine()
            println("Password:")
            val password = scanner.nextLine().encodeToByteArray()
            val encodedMessage = message.encodeToByteArray() // + byteArrayOf(0, 0, 3)
            val encryptedMessage = mutableListOf<Byte>()
            for (byteIndex in encodedMessage.indices) {
                val index = byteIndex % password.size
                val encryptedByte = encodedMessage[byteIndex] xor password[index]
                encryptedMessage.add(encryptedByte)
            }
            encryptedMessage.addAll(listOf(0, 0, 3))
            val image = ImageIO.read(inputFile)
            val size = image.width * image.height

            if (encryptedMessage.size * 8 <= size) {
                val bits = mutableListOf<Boolean>()
                encryptedMessage.forEach { byte ->
                    val newByte = mutableListOf<Boolean>()
                    repeat(8) { i ->
                        val bit = (byte / 2.0.pow(i).toInt()) % 2
                        newByte.add(bit != 0)
                    }
                    bits.addAll(newByte.reversed())
                }

                var index = 0
                iterator@ for (y in 0 until image.height) for (x in 0 until image.width) {
                    val pixel = image.getRGB(x, y)
                    val color = Color(pixel)
                    val b = if (bits[index++]) color.blue or 1 else color.blue and 0b11111110
                    val newColor = Color(color.red, color.green, b)
                    image.setRGB(x, y, newColor.rgb)
                    if (index == bits.size) break@iterator
                }
                val outputFile = File(outputImg.replace('/', File.separatorChar))
                ImageIO.write(image, "png", outputFile)
                println("Message saved in $outputImg image.")
            } else println("The input image is not large enough to hold this message.")
        } else println("Can't read input file!")
    }
}

fun main() {
    val crypto = SteganographyAndCryptography()
    crypto.cmd()
}

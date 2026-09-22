package infrastructure.vk;

/** An incoming private text message selected by the transport, not a raw VK event. */
public record VkMessage(String peerId, String text) {
}

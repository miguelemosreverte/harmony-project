package harmonia.live

enum ConnectionState:
  case Connecting, Connected
  case Disconnected(message: String)
  def label: String = this match
    case Connecting            => "Connecting"
    case Connected             => "Connected"
    case Disconnected(message) => message

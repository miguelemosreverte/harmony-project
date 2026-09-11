package harmonia.live

enum ConnectionState:
  case Connecting, Connected, SessionRequired
  case Disconnected(message: String)
  def label: String = this match
    case Connecting            => "Connecting"
    case Connected             => "Connected"
    case SessionRequired       => "Open a current participant link"
    case Disconnected(message) => message

package harmonia.live

enum ConnectionState:
  case Connecting, Connected, SessionRequired
  case Disconnected(message: String)
  def label: String = this match
    case Connecting            => "Connecting"
    case Connected             => "Connected"
    case SessionRequired       => "Reopen your workspace"
    case Disconnected(message) => message

//
//  SocketIOViewModel.swift
//
//  Created by Daksh on 31/08/20.
//  Copyright © 2020 The One Technologies. All rights reserved.
//

import Foundation

@objcMembers public class SocketIOViewModel : NSObject {
    
    let manager: SocketIOManager

    init(_manager: SocketIOManager) {
        self.manager = _manager
    }
    
    deinit {
        debugPrint("\(self):\(#function)")
    }
    
}

extension SocketIOViewModel {
    //MARK: - Send Message

    func sendMessage(event: SocketIOManager.AppEvent, message: SOMessage) {
        debugPrint("\(self):\(#function) event: \(event.name), message:\(message.toJSON())")

        manager.socket.emitWithAck(event.name, message.toJSON())
            .timingOut(after: 1) { results in
                debugPrint("\(self):\(#function): sent")
            }
    }
    
    public func updateCaptionStatus(enable: Bool) {
        debugPrint("\(self):\(#function)")

        let message = SOMessage()
        message.id = manager.parseId
        message.msg = enable ? .start : .stop
        sendMessage(event: .liveCaption, message: message)
    }
}


extension Encodable {
    //MARK: -

    func toJSONData(outputFormatting: JSONEncoder.OutputFormatting = .prettyPrinted) -> Data? {
        let encoder = JSONEncoder()
        encoder.outputFormatting = outputFormatting
        return try? encoder.encode(self)
    }

    func toJSON(outputFormatting: JSONEncoder.OutputFormatting = .prettyPrinted) -> [String: Any] {
        return self.toJSONData(outputFormatting: outputFormatting)?.toJsonObject() as? [String: Any] ?? [:]
    }

}

extension Data {
    //MARK: -
    
    func toJsonObject(withReadingOptions options: JSONSerialization.ReadingOptions = .mutableContainers) -> Any? {
        let dictJson = try? JSONSerialization.jsonObject(with: self, options: options)
        return dictJson
    }
}

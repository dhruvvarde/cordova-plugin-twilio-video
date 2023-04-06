//
//  SocketIOManager+Functions.swift
//
//  Created by Daksh on 31/08/20.
//  Copyright © 2020 The One Technologies. All rights reserved.
//

import Foundation

extension SocketIOManager {
    
    func establishConnection(with completion: SocketDidChangeConnectionState? = nil) {
        debugPrint("\(#function)")
        didChangeConnectionState = completion
        socket.connect()
    }
    
    func closeConnection(with completion: SocketDidChangeConnectionState? = nil) {
        debugPrint("\(#function)")
        didChangeConnectionState = completion
        socket.disconnect()
    }
    
    func closeEvents(events: [SocketIOManager.AppEvent]) {
        debugPrint("\(#function) events: \(events.map({$0.name})))")
        
        events.forEach { (e) in
            socket.off(e.name)
        }
    }
    
    public func clear() {
        debugPrint("\(self):\(#function)")
        closeEvents(events: observedEvents)
        closeConnection()
    }
}

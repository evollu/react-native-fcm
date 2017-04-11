declare module "react-native-fcm" {

    type FCMEventType = "FCMTokenRefreshed" | "FCMNotificationReceived";
    export module FCMEvent {
        const RefreshToken = "FCMTokenRefreshed";
        const Notification = "FCMNotificationReceived";
    }

    export module RemoteNotificationResult {
        const NewData = "UIBackgroundFetchResultNewData";
        const NoData = "UIBackgroundFetchResultNoData";
        const ResultFailed = "UIBackgroundFetchResultFailed";
    }

    export module WillPresentNotificationResult {
        const All = "UNNotificationPresentationOptionAll";
        const None = "UNNotificationPresentationOptionNone";
    }

    export module NotificationType {
        const Remote = "remote_notification";
        const NotificationResponse = "notification_response";
        const WillPresent = "will_present_notification";
        const Local = "local_notification";
    }

    export interface Subscription {
        remove(): void;
    }

    export class FCM {
        static requestPermissions(): void;
        static getFCMToken(): Promise<string>;
        static on(event: "FCMTokenRefreshed", handler: (token: string) => void): Subscription;
        static on(event: "FCMNotificationReceived", handler: (notification: any) => void): Subscription;
        static subscribeToTopic(topic: string): void;
        static unsubscribeFromTopic(topic: string): void;
        static getInitialNotification(): Promise<any>;
        static presentLocalNotification(notification: any): void;

        static scheduleLocalNotification(schedule: any): void;
        static getScheduledLocalNotifications(): Promise<any>;

        static removeAllDeliveredNotifications(): void;
        static removeDeliveredNotification(id: string): void;

        static cancelAllLocalNotifications(): void;
        static cancelLocalNotification(id: string): string;

        static setBadgeNumber(badge: number): void;
        static getBadgeNumber(): Promise<number>;
        static send(id: string, data: any): void;
    }

    export default FCM;
}

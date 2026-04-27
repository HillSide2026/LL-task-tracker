/* eslint-disable no-undef */
import { AdminState, getAdminStateLabel } from './adminLifecycle'

test('maps legacy and maintenance admin states to display labels', () => {
  expect(getAdminStateLabel('Open')).toEqual(AdminState.Opened)
  expect(getAdminStateLabel(AdminState.MaintenanceLawyerReview)).toEqual(
    AdminState.MaintenanceLawyerReview,
  )
  expect(getAdminStateLabel(AdminState.MaintenanceClientWait)).toEqual(
    AdminState.MaintenanceClientWait,
  )
  expect(getAdminStateLabel(AdminState.Active)).toEqual(AdminState.Active)
  expect(getAdminStateLabel(AdminState.ClosingReview)).toEqual(
    AdminState.ClosingReview,
  )
  expect(getAdminStateLabel(AdminState.Archived)).toEqual(AdminState.Archived)
})

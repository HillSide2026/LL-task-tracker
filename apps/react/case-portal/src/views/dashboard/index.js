import { Grid, Typography } from '@mui/material'
import DashboardCard from 'components/cards/DashboardCard'
import {
  IconAlertTriangle,
  IconArchive,
  IconChecklist,
  IconList,
  IconScale,
  IconSquareAsterisk,
} from '@tabler/icons-react'
import { useTranslation } from 'react-i18next'

const DashboardDefault = () => {
  const { t } = useTranslation()

  return (
    <Grid container rowSpacing={4.5} columnSpacing={2.75}>
      {/* row 1 */}
      <Grid item xs={12} sx={{ mb: -2.25 }}>
        <Typography variant='h5'>{t('pages.dashboard.title')}</Typography>
      </Grid>
      <Grid item xs={12} sm={6} md={4} lg={3}>
        <DashboardCard
          title={t('pages.dashboard.cards.wipcases.label')}
          icon={<IconArchive />}
          to='/case-list/wip-cases'
        />
      </Grid>
      <Grid item xs={12} sm={6} md={4} lg={3}>
        <DashboardCard
          title={t('pages.dashboard.cards.caselist.label')}
          icon={<IconSquareAsterisk />}
          to='/case-list/cases'
        />
      </Grid>
      <Grid item xs={12} sm={6} md={4} lg={3}>
        <DashboardCard
          title={t('pages.dashboard.cards.tasklist.label')}
          icon={<IconList />}
          to='/task-list'
        />
      </Grid>
      <Grid item xs={12} sm={6} md={4} lg={3}>
        <DashboardCard
          title='Intake Queue'
          icon={<IconChecklist />}
          to='/case-list/admin-opening/intake'
        />
      </Grid>
      <Grid item xs={12} sm={6} md={4} lg={3}>
        <DashboardCard
          title='Lawyer Review'
          icon={<IconScale />}
          to='/case-list/admin-opening/lawyer-review'
        />
      </Grid>
      <Grid item xs={12} sm={6} md={4} lg={3}>
        <DashboardCard
          title='Exceptions'
          icon={<IconAlertTriangle />}
          to='/case-list/admin-opening/exceptions'
        />
      </Grid>
      <Grid item xs={12} sm={6} md={4} lg={3}>
        <DashboardCard
          title='Active Matters'
          icon={<IconArchive />}
          to='/case-list/admin-maintenance/active'
        />
      </Grid>
      <Grid item xs={12} sm={6} md={4} lg={3}>
        <DashboardCard
          title='Client Waiting'
          icon={<IconChecklist />}
          to='/case-list/admin-maintenance/client-waiting'
        />
      </Grid>
      <Grid item xs={12} sm={6} md={4} lg={3}>
        <DashboardCard
          title='External Waiting'
          icon={<IconSquareAsterisk />}
          to='/case-list/admin-maintenance/external-waiting'
        />
      </Grid>
      <Grid item xs={12} sm={6} md={4} lg={3}>
        <DashboardCard
          title='Lawyer Response'
          icon={<IconScale />}
          to='/case-list/admin-maintenance/lawyer-response'
        />
      </Grid>
      <Grid item xs={12} sm={6} md={4} lg={3}>
        <DashboardCard
          title='Maintenance Exceptions'
          icon={<IconAlertTriangle />}
          to='/case-list/admin-maintenance/exceptions'
        />
      </Grid>

      <Grid
        item
        md={8}
        sx={{ display: { sm: 'none', md: 'block', lg: 'none' } }}
      />
    </Grid>
  )
}

export default DashboardDefault
